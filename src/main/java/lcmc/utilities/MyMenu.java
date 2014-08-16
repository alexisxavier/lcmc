/*
 * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
 * written by Rasto Levrinc.
 *
 * Copyright (C) 2009, LINBIT HA-Solutions GmbH.
 * Copyright (C) 2011-2012, Rastislav Levrinc.
 *
 * DRBD Management Console is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * DRBD Management Console is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with drbd; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package lcmc.utilities;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import lcmc.model.AccessMode;
import lcmc.model.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This is a menu object that holds MyMenuItems.
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MyMenu extends JMenu implements UpdatableItem {
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Position of the menu that can be stored and retrieved. */
    private Point2D pos = null;
    /** Access Type for this component to become enabled. */
    private AccessMode enableAccessMode;
    @Autowired
    private Application application;

    private EnablePredicate enablePredicate = new EnablePredicate() {
        @Override
        public String check() {
            return null;
        }
    };

    private Runnable update = new Runnable() {
        @Override
        public void run() {
            updateMenuComponents();
            processAccessMode();
        }
    };

    void init(final String text, final AccessMode enableAccessMode, final AccessMode visibleAccessMode) {
        super.setText(text);
        this.enableAccessMode = enableAccessMode;
        setOpaque(false);
        setEnabled(false);
    }

    /** Stores the position. */
    @Override
    public final void setPos(final Point2D pos) {
        this.pos = pos;
    }

    /** Gets the position. */
    public final Point2D getPos() {
        return pos;
    }

    /**
     * This function is usually overriden and is called when the menu and its
     * items are to be updated.
     */
    @Override
    public void updateAndWait() {
        update.run();
    }

    public void updateMenuComponents() {
        final Collection<java.awt.Component> copy = new ArrayList<java.awt.Component>();
        for (final java.awt.Component m : getMenuComponents()) {
            copy.add(m);
        }
        for (final java.awt.Component m : copy) {
            if (m instanceof UpdatableItem) {
                ((UpdatableItem) m).updateAndWait();
            }
        }
    }

    /** Sets this item enabled and visible according to its access type. */
    public void processAccessMode() {
        final boolean accessible =
                   application.isAccessible(enableAccessMode);
        final String disableTooltip = enablePredicate.check();
        setEnabled(disableTooltip == null && accessible);
        if (isVisible()) {
            if (!accessible && enableAccessMode.getAccessType()
                               != Application.AccessType.NEVER) {
                String advanced = "";
                if (enableAccessMode.isAdvancedMode()) {
                    advanced = "Advanced ";
                }
                setToolTipText("<html><b>"
                               + getText()
                               + " (disabled)</b><br>available in \""
                               + advanced
                               + Application.OP_MODES_MAP.get(
                                      enableAccessMode.getAccessType())
                               + "\" mode</html>");
            } else if (disableTooltip != null) {
                setToolTipText("<html><b>"
                               + getText()
                               + " (disabled)</b><br>"
                               + disableTooltip
                               + "</html>");
            }
        }
    }

    /** Cleanup. */
    @Override
    public final void cleanup() {
        for (final java.awt.Component m : getMenuComponents()) {
            if (m instanceof UpdatableItem) {
                ((UpdatableItem) m).cleanup();
            } else if (m instanceof JScrollPane) {
                ((MyList) ((JScrollPane) m).getViewport().getView()).cleanup();
            }
        }
    }

    /** Remove all items. */
    @Override
    public final void removeAll() {
        for (int i = 0; i < getItemCount(); i++) {
            final JMenuItem item = getItem(i);
            if (item instanceof MyMenuItem) {
                ((UpdatableItem) item).cleanup();
            } else if (item instanceof MyMenu) {
                item.removeAll();
            }
        }
        super.removeAll();
    }

    public MyMenu enablePredicate(final EnablePredicate enablePredicate) {
        this.enablePredicate = enablePredicate;
        return this;
    }

    public void onUpdate(final Runnable update) {
        this.update = update;
    }
}
