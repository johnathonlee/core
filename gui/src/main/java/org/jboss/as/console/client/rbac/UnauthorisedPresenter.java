package org.jboss.as.console.client.rbac;

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * @author Heiko Braun
 * @date 7/24/13
 */
public class UnauthorisedPresenter extends PresenterWidget<UnauthorisedPresenter.MyView> {

    @Inject
    public UnauthorisedPresenter(EventBus eventBus, MyView view) {
        super(eventBus, view);
    }

    public interface MyView extends View {
    }
}
