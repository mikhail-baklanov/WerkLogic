package ru.werklogic.werklogic.dm;


import java.util.ArrayList;
import java.util.List;

import ru.werklogic.werklogic.R;

/**
 * Created by bmw on 22.08.2015.
 */
public enum SensorType {
    NONE(0, 0),
    GERCON(1, 1, new ActionInfo[]{
            new ActionInfo(R.string.action, ActionType.SIGNAL)
    }),
    INFRA(2, 1, new ActionInfo[]{
            new ActionInfo(R.string.action, ActionType.SIGNAL)
    }),
    BRELOK2(3, 2, new ActionInfo[]{
            new ActionInfo(R.string.button1_action, ActionType.TO_SPY),
            new ActionInfo(R.string.button2_action, ActionType.TO_NORMAL)
    }),
    BRELOK4(4, 4, new ActionInfo[]{
            new ActionInfo(R.string.button1_action, ActionType.TO_SPY),
            new ActionInfo(R.string.button2_action, ActionType.TO_NORMAL),
            new ActionInfo(R.string.button3_action, ActionType.SWITCH),
            new ActionInfo(R.string.button4_action, ActionType.SIGNAL)
    });

    private int typeNumber;
    private int buttonsCount;
    private List<ActionInfo> defaultActionsInfo;

    SensorType(int typeNumber, int buttonsCount, ActionInfo... defaultActionsInfo) {
        this.typeNumber = typeNumber;
        this.buttonsCount = buttonsCount;
        this.defaultActionsInfo = new ArrayList<>();
        if (defaultActionsInfo != null) {
            for (ActionInfo a : defaultActionsInfo)
                this.defaultActionsInfo.add(a);
        }
    }

    public static SensorType getSensorTypeByNumber(int typeNumber) {
        for (SensorType s : values()) {
            if (s.typeNumber == typeNumber)
                return s;
        }
        return NONE;
    }

    public int getButtonsCount() {
        return buttonsCount;
    }

    public List<ActionInfo> getDefaultActionsInfo() {
        return defaultActionsInfo;
    }

    public static class ActionInfo {
        private int messageResId;
        private ActionType actionType;

        private ActionInfo(int messageResId, ActionType actionType) {
            this.messageResId = messageResId;
            this.actionType = actionType;
        }

        public int getMessageResId() {
            return messageResId;
        }

        public ActionType getActionType() {
            return actionType;
        }
    }
}
