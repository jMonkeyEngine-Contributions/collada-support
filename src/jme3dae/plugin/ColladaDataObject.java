/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3dae.plugin;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.assets.SpatialAssetDataObject;
import com.jme3.scene.Spatial;
import java.io.IOException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.util.Exceptions;

public class ColladaDataObject extends SpatialAssetDataObject {

    public ColladaDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
    }

//    @Override
    public Spatial loadAsset() {
        ProjectAssetManager mgr=getLookup().lookup(ProjectAssetManager.class);
        if (mgr == null) {
            return null;
        }
        String assetKey = mgr.getRelativeAssetPath(getPrimaryFile().getPath());
        mgr.getManager().registerLoader(jme3dae.ColladaLoader.class, "dae");
        FileLock lock = null;
        try {
            lock = getPrimaryFile().lock();
            Spatial spatial = mgr.getManager().loadModel(assetKey);
            lock.releaseLock();
            return spatial;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            if (lock != null) {
                lock.releaseLock();
            }
        }
        return null;
    }
}
