package org.jboss.as.console.client.tools;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.HasTreeItems;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.as.console.client.core.SuspendableViewImpl;
import org.jboss.as.console.client.widgets.tabs.DefaultTabLayoutPanel;
import org.jboss.dmr.client.ModelNode;
import org.jboss.dmr.client.Property;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Heiko Braun
 * @date 6/15/12
 */
public class BrowserView extends SuspendableViewImpl implements BrowserPresenter.MyView {

    private BrowserPresenter presenter;
    private SplitLayoutPanel layout;

    private VerticalPanel treeContainer;
    private RawView rawView;
    private Tree tree;
    private DescriptionView descView;

    @Override
    public void setPresenter(BrowserPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Widget createWidget() {

        tree = new Tree();
        tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> selection) {
                final LinkedList<String> path = resolvePath(selection.getSelectedItem());
                System.out.println(">> "+ path +" :: "+ toAddress(path));

                rawView.clearDisplay();
                descView.clearDisplay();
                if(path.size()%2==0)
                    presenter.readResource(toAddress(path));
            }
        });

        layout = new SplitLayoutPanel(10);
        treeContainer = new VerticalPanel();
        treeContainer.setStyleName("fill-layout");
        treeContainer.add(tree);

        ScrollPanel scroll = new ScrollPanel(treeContainer);
        layout.addWest(scroll, 300);

        rawView = new RawView();
        descView = new DescriptionView();


        DefaultTabLayoutPanel tabLayoutPanel = new DefaultTabLayoutPanel(40, Style.Unit.PX);
                tabLayoutPanel.addStyleName("default-tabpanel");
        layout.add(tabLayoutPanel);

        tabLayoutPanel.add(descView.asWidget(), "Description");
        tabLayoutPanel.add(rawView.asWidget(), "Model");
        tabLayoutPanel.selectTab(0);

        tree.addOpenHandler(new OpenHandler<TreeItem>() {
            @Override
            public void onOpen(OpenEvent<TreeItem> event) {
                onItemOpenend(event.getTarget());
            }
        });

        return layout;
    }

    private void onItemOpenend(TreeItem treeItem) {

        // check if it has a placeholder child
        if(treeItem.getChildCount()>0 && (treeItem.getChild(0) instanceof PlaceholderItem))
        {
            final List<String> path = resolvePath(treeItem.getChild(0));

            if(path.size()%2==0)
                presenter.readChildrenNames(toAddress(path));
            else
                presenter.readChildrenTypes(toAddress(path));

        }
    }

    public static ModelNode toAddress(List<String> path)
    {

        ModelNode address = new ModelNode();
        address.setEmptyList();

        if(path.size()<2) return address;

        for(int i=1; i<path.size();i+=2)
        {
            if(i%2!=0 )
                address.add(path.get(i-1), path.get(i));
            else
                address.add(path.get(i), "*");
        }

        return address;

    }

    @Override
    public void updateChildrenTypes(ModelNode address, List<ModelNode> modelNodes) {

        HasTreeItems rootItem = null;

        if(address.asList().isEmpty())
        {
            tree.clear();
            rootItem = tree;
        }
        else
        {
            rootItem = findTreeItem(tree, address);
        }

        addChildrenTypes(rootItem, modelNodes);

    }

    @Override
    public void updateChildrenNames(ModelNode address, List<ModelNode> modelNodes) {

        TreeItem rootItem = findTreeItem(tree, address);

        assert rootItem!=null : "unable to find matching tree item: "+address;

        addChildrenNames(rootItem, modelNodes);

    }

    @Override
    public void updateDescription(ModelNode address, ModelNode description) {
        descView.updateDescription(address, description);
    }

    @Override
    public void updateResource(ModelNode address, ModelNode resource) {

        final List<Property> tokens = address.asPropertyList();
        String name = tokens.get(tokens.size()-1).getValue().asString();
        rawView.display(new Property(name, resource));
    }

    private void addChildrenTypes(HasTreeItems rootItem, List<ModelNode> modelNodes) {

        rootItem.removeItems();

        for(ModelNode child : modelNodes)
        {
            TreeItem childItem = new TreeItem(child.asString());
            childItem.addItem(new PlaceholderItem());
            rootItem.addItem(childItem);
        }
    }

    private void addChildrenNames(TreeItem rootItem, List<ModelNode> modelNodes) {

        rootItem.removeItems();

        if(modelNodes.isEmpty())
            rootItem.addItem(new PlaceholderItem());

        for(ModelNode child : modelNodes)
        {
            TreeItem childItem = new TreeItem(child.asString());
            childItem.addItem(new PlaceholderItem());
            rootItem.addItem(childItem);
        }
    }

    private static TreeItem findTreeItem(Tree root, ModelNode address) {

        LinkedList<String> path = new LinkedList<String>();
        for(Property prop : address.asPropertyList())
        {
            path.add(prop.getName());
            final String value = prop.getValue().asString();
            if(!"*".equals(value)) {
                path.add(value);
            }
        }

        final Iterator<String> iterator = path.iterator();

        TreeItem next = null;

        if(iterator.hasNext())
        {
            final String pathName = iterator.next();
            for(int i=0; i<root.getItemCount(); i++)
            {
                if(root.getItem(i).getText().equals(pathName))
                {
                    next = root.getItem(i);
                    break;
                }
            }
        }

        if(next==null)
            return null;
        else if (next!=null && !iterator.hasNext())
            return next;
        else
            return findTreeItem(next, iterator);
    }

    private static TreeItem findTreeItem(TreeItem root, Iterator<String> iterator)
    {
        TreeItem next = null;
        if(iterator.hasNext())
        {
            final String pathName = iterator.next();
            for(int i=0; i<root.getChildCount(); i++)
            {

                if(root.getChild(i).getText().equals(pathName))
                {
                    next = root.getChild(i);
                    break;
                }
            }
        }

        if(next==null)
            return null;
        else if (next!=null && !iterator.hasNext())
            return next;
        else
            return findTreeItem(next, iterator);

    }

    public static LinkedList<String> resolvePath(TreeItem item)
    {
        LinkedList<String> address = new LinkedList<String>();
        recurseToTop(item, address);

        return address;
    }

    private static void recurseToTop(TreeItem item, LinkedList<String> address)
    {
        address.addFirst(item.getText());

        if(item.getParentItem()!=null)
        {
            recurseToTop(item.getParentItem(), address);
        }
    }

    class PlaceholderItem extends TreeItem {
        PlaceholderItem() {
            super("*");
        }
    }
}