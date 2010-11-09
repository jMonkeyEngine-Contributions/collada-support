/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3dae.plugin;

import com.jme3.asset.AssetManager;
import com.jme3.gde.core.assets.AssetManagerConfigurator;

/**
 *
 * @author normenhansen
 */
@org.openide.util.lookup.ServiceProvider(service = AssetManagerConfigurator.class)
public class ColladaAssetManagerConfigurator implements AssetManagerConfigurator {

    public void prepareManager(AssetManager manager) {
        manager.registerLoader(jme3dae.ColladaLoader.class, "dae");
    }
}
