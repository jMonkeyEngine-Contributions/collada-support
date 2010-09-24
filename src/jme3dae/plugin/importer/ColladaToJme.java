/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3dae.plugin.importer;

import com.jme3.asset.DesktopAssetManager;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.gde.core.assets.AssetProperties;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.scene.Spatial;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

public final class ColladaToJme implements ActionListener {

    private final DataObject context;

    public ColladaToJme(DataObject context) {
        this.context = context;
    }

    public void actionPerformed(ActionEvent ev) {
        final ProjectAssetManager manager = context.getLookup().lookup(ProjectAssetManager.class);
        if (manager == null) {
            StatusDisplayer.getDefault().setStatusText("Project has no AssetManager!");
            return;
        }

        if (context != null) {
            Runnable run = new Runnable() {

                public void run() {
                    ProgressHandle progressHandle = ProgressHandleFactory.createHandle("Converting Collada");
                    progressHandle.start();

                    FileObject file = context.getPrimaryFile();
                    FileLock lock = null;
                    try {
                        lock = file.lock();
                        String outputPath = file.getParent().getPath() + File.separator + file.getName() + ".j3o";
                        //we have to create our own asset manager, the core does not know about the loader class
                        DesktopAssetManager mgr=new DesktopAssetManager(true);
                        mgr.registerLocator(manager.getProject().getProjectDirectory().getPath() + "/" + manager.getFolderName() + "/",
                                "com.jme3.asset.plugins.FileLocator");
                        mgr.registerLoader(jme3dae.ColladaLoader.class, "dae");
                        //load model
                        Spatial model = mgr.loadModel(manager.getRelativeAssetPath(file.getPath()));
                        BinaryExporter exp = BinaryExporter.getInstance();
                        File outFile=new File(outputPath);
                        exp.save(model, outFile);
                        DataObject targetModel=DataObject.find(FileUtil.toFileObject(outFile));
                        AssetProperties properties=targetModel.getLookup().lookup(AssetProperties.class);
                        if(properties!=null){
                            properties.loadProperties();
                            properties.setProperty("ORIGINAL_PATH", manager.getRelativeAssetPath(file.getPath()));
                            properties.saveProperties();
                        }
                        StatusDisplayer.getDefault().setStatusText("Created file " + file.getName() + ".j3o");
                        //try make NetBeans update the tree.. :/
                        context.getPrimaryFile().getParent().refresh();
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                        Confirmation msg = new NotifyDescriptor.Confirmation(
                                "Error converting " + file.getNameExt() + "\n" + ex.toString(),
                                NotifyDescriptor.OK_CANCEL_OPTION,
                                NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(msg);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        progressHandle.finish();
                    }
                }
            };
            new Thread(run).start();
        }

        StatusDisplayer.getDefault().setStatusText("Import with project AssetManager: " + manager);
    }
}
