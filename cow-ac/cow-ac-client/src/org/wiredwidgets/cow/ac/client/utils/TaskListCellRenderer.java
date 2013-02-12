package org.wiredwidgets.cow.ac.client.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import org.wiredwidgets.cow.ac.client.controllers.TaskController;
import org.wiredwidgets.cow.server.api.service.Task;

/**
 * Decorates <code>Task</code>s a bit nicer and distinguishes between assigned and
 * available tasks. Could do much more.
 *
 * @author RYANMILLER
 * @see org.wiredwidgets.cow.ac.client.ui.TasksPanel
 */
public class TaskListCellRenderer extends JComponent
        implements ListCellRenderer {

    static Color listForeground, listBackground,
            listSelectionForeground, listSelectionBackground;

    static {
        UIDefaults uid = UIManager.getLookAndFeel().getDefaults();
        listForeground = uid.getColor("List.foreground");
        listBackground = uid.getColor("List.background");
        listSelectionForeground = uid.getColor("List.selectionForeground");
        listSelectionBackground = uid.getColor("List.selectionBackground");
    }
    JLabel taskInfo;
    JCheckBox checkbox;
    Icon myTaskIcon;
    Icon availableTaskIcon;

    /**
     * Creates the CellRenderer and loads image resources. This initialize can
     * cause strange problems if the paths to the image resources aren't correct.
     */
    public TaskListCellRenderer() {
        setLayout(new BorderLayout());
        myTaskIcon = new ImageIcon(
                getClass().getResource("/org/wiredwidgets/cow/ac/client/images/assigned-task-icon.png"));
        availableTaskIcon = new ImageIcon(
                getClass().getResource("/org/wiredwidgets/cow/ac/client/images/available-task-icon_o.png"));

        // rdm 12/8/2011 Checkbox not used for now, so hiding
        //checkbox = new JCheckBox();
        //checkbox.setOpaque(true);
        taskInfo = new JLabel();
        taskInfo.setOpaque(true); // needed to make the background color show up

        //add(checkbox, BorderLayout.WEST);
        add(taskInfo, BorderLayout.WEST);  // move to center if using checkbox
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {

        if (value instanceof Task) // use the name of the task as the primary display element
        {
            Task t = (Task) value;
            taskInfo.setText(t.getProcessInstanceId() + ": " + t.getName());

            // denote the task with some special markings
            if (TaskController.getInstance().getSpecializedTasksModel().isAvailableTask(t)) {
                taskInfo.setText(taskInfo.getText() + " (available)");
                taskInfo.setIcon(availableTaskIcon);
            } else {
                taskInfo.setIcon(myTaskIcon);
            }

        } else // allows the list to display other objects in a very basic way that might get thrown in
        {
            taskInfo.setText(value.toString());
        }

        // Don't actually want it to be selected when it's selected, as a check would indicate completeness
        //checkbox.setSelected(isSelected);

        // clean up coloring
        Component[] comps = getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (isSelected) {
                comps[i].setForeground(listSelectionForeground);
                comps[i].setBackground(listSelectionBackground);
            } else {
                comps[i].setForeground(listForeground);
                comps[i].setBackground(listBackground);
            }
        }

        return this;
    }
}