package org.csstudio.saverestore.ui.browser;

import java.util.List;

import org.csstudio.saverestore.SearchCriterion;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 *
 * <code>SearchForSnapshotsCommand</code> is the comment handler for searching repository for the snapshots.
 *
 * @author <a href="mailto:jaka.bobnar@cosylab.com">Jaka Bobnar</a>
 *
 */
public class SearchForSnapshotsCommand extends AbstractHandler implements IHandler {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        final IWorkbenchPart part = HandlerUtil.getActivePart(event);
        if (part instanceof BrowserView) {
            final SearchDialog dialog = SearchDialog.getSingletonInstance(part.getSite().getShell());
            dialog.openAndWait().ifPresent(expr -> {
                List<SearchCriterion> criteria = dialog.getSelectedCriteria();
                ((BrowserView) part).getActionManager().searchForSnapshots(expr, criteria,
                    snapshots -> ((BrowserView) part).setSearchResults(expr, snapshots));
            });
        }
        return null;
    }

}
