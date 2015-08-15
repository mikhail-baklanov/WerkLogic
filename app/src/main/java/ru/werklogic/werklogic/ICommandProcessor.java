package ru.werklogic.werklogic;

import ru.werklogic.werklogic.commands.BaseCommand;

/**
 * Created by bmw on 20.07.2015.
 */
public interface ICommandProcessor {
    void processCommand(BaseCommand command);
}
