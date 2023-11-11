/*
 * Copyright (C) 2019 BARBOTIN Nicolas
 */

package net.montoyo.wd.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.montoyo.wd.WebDisplays;
import net.montoyo.wd.core.IComputerArgs;
import net.montoyo.wd.core.IUpgrade;
import net.montoyo.wd.core.ScreenRights;
import net.montoyo.wd.utilities.*;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Map;

public abstract class AbstractInterfaceBlockEntity extends AbstractPeripheralBlockEntity {
    public AbstractInterfaceBlockEntity(BlockEntityType<?> arg, BlockPos arg2, BlockState arg3) {
        super(arg, arg2, arg3);
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ComputerFunc {}

    private NameUUIDPair owner;
    private static final Object[] TRUE = new Object[] { true };
    private static final Object[] FALSE = new Object[] { false };
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        owner = Util.readOwnerFromNBT(tag);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        Util.writeOwnerToNBT(tag, owner);
    }
    
    public void setOwner(Player ep) {
        owner = new NameUUIDPair(ep.getGameProfile());
        setChanged();
    }

    @ComputerFunc
    public Object[] isLinked(IComputerArgs args) {
        return new Object[] { isLinked() };
    }

    @ComputerFunc
    public Object[] isScreenChunkLoaded(IComputerArgs args) {
        return new Object[] { isScreenChunkLoaded() };
    }

    @ComputerFunc
    public Object[] getScreenPos(IComputerArgs args) {
        return isLinked() ? new Object[] { screenPos.x, screenPos.y, screenPos.z } : null;
    }

    @ComputerFunc
    public Object[] getScreenSide(IComputerArgs args) {
        return isLinked() ? new Object[] { screenSide.toString().toLowerCase() } : null;
    }

    @ComputerFunc
    public Object[] getOwner(IComputerArgs args) {
        if(owner == null)
            return null;
        else
            return new Object[] { owner.name, owner.uuid.toString() };
    }

    @ComputerFunc
    public Object[] can(IComputerArgs args) {
        String what = args.checkString(0).toLowerCase();
        int right;
        switch(what) {
            case "click":
            case "type":
                right = ScreenRights.INTERACT;
                break;

            case "seturl":
            case "js":
            case "javascript":
            case "runjs":
                right = ScreenRights.CHANGE_URL;
                break;

            case "setresolution":
            case "setrotation":
                right = ScreenRights.MODIFY_SCREEN;
                break;

            default:
                throw new IllegalArgumentException("invalid right name");
        }

        ScreenBlockEntity tes = getConnectedScreenEx();
        if(owner == null || tes == null)
            return null;
        else
            return ((tes.getScreen(screenSide).rightsFor(owner.uuid) & right) == 0) ? FALSE : TRUE;
    }

    @ComputerFunc
    public Object[] hasUpgrade(IComputerArgs args) {
        String name = args.checkString(0);

        ScreenBlockEntity tes = getConnectedScreenEx();
        if(owner == null || tes == null)
            return null;
        else
            return tes.getScreen(screenSide).upgrades.stream().anyMatch(is -> ((IUpgrade) is.getItem()).getJSName(is).equalsIgnoreCase(name)) ? TRUE : FALSE;
    }

    @ComputerFunc
    public Object[] getSize(IComputerArgs args) {
        ScreenBlockEntity tes = getConnectedScreenEx();

        if(owner == null || tes == null)
            return null;
        else {
            Vector2i sz = tes.getScreen(screenSide).size;
            return new Object[] { sz.x, sz.y };
        }
    }

    @ComputerFunc
    public Object[] getResolution(IComputerArgs args) {
        ScreenBlockEntity tes = getConnectedScreenEx();

        if(owner == null || tes == null)
            return null;
        else {
            Vector2i res = tes.getScreen(screenSide).resolution;
            return new Object[] { res.x, res.y };
        }
    }

    @ComputerFunc
    public Object[] getRotation(IComputerArgs args) {
        ScreenBlockEntity tes = getConnectedScreenEx();

        if(owner == null || tes == null)
            return null;
        else
            return new Object[] { tes.getScreen(screenSide).rotation.getAngleAsInt() };
    }

    @ComputerFunc
    public Object[] getURL(IComputerArgs args) {
        ScreenBlockEntity tes = getConnectedScreenEx();

        if(owner == null || tes == null)
            return null;
        else
            return new Object[] { tes.getScreen(screenSide).url };
    }

    private static Object[] err(String str) {
        return new Object[] { false, str };
    }

    @ComputerFunc
    public Object[] click(IComputerArgs args) {
        int x = args.checkInteger(0);
        int y = args.checkInteger(1);
        String action = "click";
        if(args.count() > 2)
            action = args.checkString(2).toLowerCase();

//        int actionId;
//        switch(action) {
//            case "click":
//                actionId = CMessageScreenUpdate.MOUSE_CLICK;
//                break;
//
//            case "up":
//            case "release":
//                actionId = CMessageScreenUpdate.MOUSE_UP;
//                break;
//
//            case "down":
//            case "press":
//                actionId = CMessageScreenUpdate.MOUSE_DOWN;
//                break;
//
//            case "move":
//                actionId = CMessageScreenUpdate.MOUSE_MOVE;
//                break;
//
//            default:
//                throw new IllegalArgumentException("bad action name");
//        }

        ScreenBlockEntity scr = getConnectedScreenEx();

        if(owner == null || scr == null)
            return err("notlinked");
        else {
            ScreenBlockEntity.Screen scrscr = scr.getScreen(screenSide);

            if((scrscr.rightsFor(owner.uuid) & ScreenRights.INTERACT) == 0)
                return err("restrictions");
            else {
                switch(scrscr.rotation) {
                    case ROT_90:
                        y = scrscr.resolution.y - y;
                        break;

                    case ROT_180:
                        x = scrscr.resolution.x - x;
                        y = scrscr.resolution.y - y;
                        break;

                    case ROT_270:
                        x = scrscr.resolution.x - x;
                        break;

                    default:
                        break;
                }

//                if(scrscr.rotation.isVertical)
//                    scr.clickUnsafe(screenSide, actionId, y, x);
//                else
//                    scr.clickUnsafe(screenSide, actionId, x, y);

                return TRUE;
            }
        }
    }

    private Object[] realType(String what) {
        ScreenBlockEntity scr = getConnectedScreenEx();

        if(owner == null || scr == null)
            return err("notlinked");
        else if((scr.getScreen(screenSide).rightsFor(owner.uuid) & ScreenRights.INTERACT) == 0)
            return err("restrictions");
        else {
            scr.type(screenSide, what, null);
            return TRUE;
        }
    }

    @ComputerFunc
    public Object[] type(IComputerArgs args) {
        String text = args.checkString(0);
        if(text.length() > 64)
            return err("toolong");

        if(text.indexOf((char) 1) >= 0)
            return err("badchar");

        return realType("t" + text);
    }

    @ComputerFunc
    public Object[] typeAdvanced(IComputerArgs args) {
        ArrayList<TypeData> data = new ArrayList<>();
        Map map = args.checkTable(0);
        int maxEvents = 0;

        for(Object o: map.values()) {
            if(!(o instanceof Map))
                return err("badinput");

            if(++maxEvents >= 16)
                return err("toomany");

            Map event = (Map) o;
            Object action = event.get("action");
            Object chr = event.get("char");
            int code = 0;

            if(!(action instanceof String))
                return err("badaction");

            if(!(chr instanceof String))
                return err("badchar");

            String strAction = (String) action;
            String strChr = (String) chr;
            TypeData.Action dataAction;

            if(strAction.equalsIgnoreCase("press"))
                dataAction = TypeData.Action.PRESS;
            else if(strAction.equalsIgnoreCase("release"))
                dataAction = TypeData.Action.RELEASE;
            else if(strAction.equalsIgnoreCase("type"))
                dataAction = TypeData.Action.TYPE;
            else
                return err("unknownaction");

            if(strChr.isEmpty())
                return err("emptychar");

            if(dataAction != TypeData.Action.TYPE) {
                Object oCode = event.get("code");
                if(!(oCode instanceof Double))
                    return err("badcode");

                code = ((Double) oCode).intValue();
            }

            data.add(new TypeData(dataAction, code, 0, 0));
        }

        return realType(WebDisplays.GSON.toJson(data));
    }

    @ComputerFunc
    public Object[] setURL(IComputerArgs args) {
        String url = args.checkString(0);
        ScreenBlockEntity scr = getConnectedScreenEx();

        if(owner == null || scr == null)
            return err("notlinked");
        else if((scr.getScreen(screenSide).rightsFor(owner.uuid) & ScreenRights.CHANGE_URL) == 0)
            return err("restrictions");
        else {
            try {
                scr.setScreenURL(screenSide, url);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return TRUE;
        }
    }

    @ComputerFunc
    public Object[] setResolution(IComputerArgs args) {
        int rx = args.checkInteger(0);
        int ry = args.checkInteger(1);
        ScreenBlockEntity scr = getConnectedScreenEx();

        if(owner == null || scr == null)
            return err("notlinked");
        else if((scr.getScreen(screenSide).rightsFor(owner.uuid) & ScreenRights.MODIFY_SCREEN) == 0)
            return err("restrictions");
        else {
            scr.setResolution(screenSide, new Vector2i(rx, ry));
            return TRUE;
        }
    }

    @ComputerFunc
    public Object[] setRotation(IComputerArgs args) {
        int rot = args.checkInteger(0);
        if(rot < 0) {
            int toAdd = (rot / -360) + 1;
            rot += toAdd * 360;
        }

        rot /= 90;
        rot &= 3;

        ScreenBlockEntity scr = getConnectedScreenEx();

        if(owner == null || scr == null)
            return err("notlinked");
        else if((scr.getScreen(screenSide).rightsFor(owner.uuid) & ScreenRights.MODIFY_SCREEN) == 0)
            return err("restrictions");
        else {
            scr.setRotation(screenSide, Rotation.values()[rot]);
            return TRUE;
        }
    }

    @ComputerFunc
    public Object[] runJS(IComputerArgs args) {
        String code = args.checkString(0);
        ScreenBlockEntity scr = getConnectedScreenEx();

        if(owner == null || scr == null)
            return err("notlinked");
        else if((scr.getScreen(screenSide).rightsFor(owner.uuid) & ScreenRights.CHANGE_URL) == 0)
            return err("restrictions");
        else {
//            scr.evalJS(screenSide, code);
            return TRUE;
        }
    }

    @ComputerFunc
    public Object[] unlink(IComputerArgs args) {
        if(isLinked()) {
            screenPos = null;
            screenSide = null;
            setChanged();
        }

        return null;
    }
}
