package org.jboss.as.console.client.analytics;

import com.google.gwt.core.client.GWT;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.googleanalytics.GoogleAnalytics;
import org.jboss.as.console.client.ProductConfig;
import org.jboss.as.console.client.shared.Preferences;

import javax.inject.Inject;

/**
 * @author Heiko Braun
 * @date 10/24/12
 */
public class AnalyticsProvider implements Provider<GoogleAnalytics> {

    private GoogleAnalytics delegate;
    private static final GoogleAnalytics NOOP = new NoopAnalytics();

    private ProductConfig prodConfig;

    @Inject
    public AnalyticsProvider(ProductConfig prodConfig) {
        this.prodConfig = prodConfig;
    }

    @Override
    public GoogleAnalytics get() {

        GoogleAnalytics analytics = null;

        // no preferences == enabled
        boolean prefEnabled = !Preferences.has(Preferences.Key.ANALYTICS)
                || Preferences.get(Preferences.Key.ANALYTICS).equals("true");

        // disabled for EAP by default
        boolean isEAP = ProductConfig.Profile.EAP.equals(prodConfig.getProfile());

        // web mode only
        boolean isWebMode = GWT.isScript();

        if(!isEAP && prefEnabled && isWebMode)
        {
            analytics = new CustomAnalyticsImpl();
            System.out.println("Google analytics is setup");
        }
        else
        {
            System.out.println("Running stub analytics implementation");
            analytics = NOOP;
        }

        return analytics;
    }
    
    static class NoopAnalytics implements GoogleAnalytics
    {
        @Override
        public void init(String userAccount) {
            
        }

        @Override
        public void addAccount(String trackerName, String userAccount) {
            
        }

        @Override
        public void trackPageview() {
            
        }

        @Override
        public void trackPageview(String pageName) {
            
        }

        @Override
        public void trackPageview(String trackerName, String pageName) {
            
        }

        @Override
        public void trackEvent(String category, String action) {
            
        }

        @Override
        public void trackEventWithTracker(String trackerName, String category, String action) {
            
        }

        @Override
        public void trackEvent(String category, String action, String optLabel) {
            
        }

        @Override
        public void trackEventWithTracker(String trackerName, String category, String action, String optLabel) {
            
        }

        @Override
        public void trackEvent(String category, String action, String optLabel, int optValue) {
            
        }

        @Override
        public void trackEventWithTracker(String trackerName, String category, String action, String optLabel, int optValue) {
            
        }

        @Override
        public void trackEvent(String category, String action, String optLabel, int optValue, boolean optNonInteraction) {
            
        }

        @Override
        public void trackEventWithTracker(String trackerName, String category, String action, String optLabel, int optValue, boolean optNonInteraction) {
            
        }
    }
}
