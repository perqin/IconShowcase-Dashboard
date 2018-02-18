/*
 * Copyright (c) 2017 Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Special thanks to the project contributors and collaborators
 * 	https://github.com/jahirfiquitiva/IconShowcase#special-thanks
 */

package jahirfiquitiva.iconshowcase.tasks;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.fragments.MainFragment;
import jahirfiquitiva.iconshowcase.holders.lists.FullListHolder;
import jahirfiquitiva.iconshowcase.models.KustomKomponent;
import jahirfiquitiva.iconshowcase.models.KustomWallpaper;
import jahirfiquitiva.iconshowcase.models.KustomWidget;
import jahirfiquitiva.iconshowcase.utilities.utils.IconUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.Utils;
import timber.log.Timber;

public class LoadKustomFiles extends AsyncTask<Void, String, Boolean> {

    private final ArrayList<KustomKomponent> komponents = new ArrayList<>();
    private final ArrayList<KustomWallpaper> wallpapers = new ArrayList<>();
    private final ArrayList<KustomWidget> widgets = new ArrayList<>();
    private final WeakReference<Context> context;

    public LoadKustomFiles(Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected Boolean doInBackground(Void... params) {
        boolean worked = false;
        try {
            AssetManager assetManager = context.get().getAssets();
            String[] kustomFolders = {"komponents", "wallpapers", "widgets"};
            for (String kustomFolder : kustomFolders) {
                boolean partialWorked = readKustomFiles(assetManager, kustomFolder);
                if (!worked)
                    worked = partialWorked;
            }
        } catch (Exception e) {
            Log.e("Kustom", e.getMessage());
            worked = false;
        }
        return worked;
    }

    @Override
    protected void onPostExecute(Boolean worked) {
        if (worked) {
            FullListHolder.get().kustomWidgets().createList(widgets);
            FullListHolder.get().komponents().createList(komponents);
            FullListHolder.get().kustomWalls().createList(wallpapers);
            if (context.get() instanceof ShowcaseActivity) {
                if (((ShowcaseActivity) context.get()).getCurrentFragment() instanceof
                        MainFragment) {
                    ((MainFragment) ((ShowcaseActivity) context.get()).getCurrentFragment())
                            .updateAppInfoData();
                }
            }
        } else {
            Timber.e("Something went really wrong while loading Kustom files");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean readKustomFiles(AssetManager assetManager, String folder) {
        try {
            String[] kustomFiles = assetManager.list(folder);
            File previewsFolder = new File(context.get().getCacheDir(),
                    IconUtils.capitalizeText(folder) + "Previews");
            if (kustomFiles != null && kustomFiles.length > 0) {
                Utils.clean(previewsFolder);
                previewsFolder.mkdirs();
                for (String template : kustomFiles) {
                    File widgetPreviewFile = new File(previewsFolder, template);
                    String widgetName = Utils.getFilenameWithoutExtension(template);
                    String[] previews = getWidgetPreviewPathFromZip(widgetName, folder,
                            assetManager.open(folder + "/" + template), previewsFolder,
                            widgetPreviewFile);
                    if (previews != null) {
                        switch (folder) {
                            case "komponents":
                                komponents.add(new KustomKomponent(previews[0]));
                                break;
                            case "wallpapers":
                                wallpapers.add(new KustomWallpaper(template, previews[0],
                                        previews[1]));
                                break;
                            case "widgets":
                                widgets.add(new KustomWidget(template, previews[0], previews[1]));
                                break;
                        }
                    }
                    widgetPreviewFile.delete();
                }
                switch (folder) {
                    case "komponents":
                        return kustomFiles.length == komponents.size();
                    case "wallpapers":
                        return kustomFiles.length == wallpapers.size();
                    case "widgets":
                        return kustomFiles.length == widgets.size();
                    default:
                        return false;
                }
            }
        } catch (Exception ex) {
            Log.e("Kustom", ex.getMessage());
        }
        return false;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "ThrowFromFinallyBlock"})
    private String[] getWidgetPreviewPathFromZip(String oldName, String folder, InputStream in,
                                                 File previewsFolder, File widgetPreviewFile) {

        OutputStream out;
        String name = oldName.replaceAll(".komp", "").replaceAll(".kwgt", "")
                .replaceAll(".klwp", "");

        String[] thumbNames = {"", ""};
        switch (folder) {
            case "komponents":
                thumbNames[0] = "komponent_thumb";
                break;
            default:
                thumbNames[0] = "preset_thumb_portrait";
                thumbNames[1] = "preset_thumb_landscape";
                break;
        }

        File preview1 = new File(previewsFolder, name + "_port.jpg");
        File preview2 = new File(previewsFolder, name + "_land.jpg");

        try {
            out = new FileOutputStream(widgetPreviewFile);
            Utils.copyFiles(in, out);
            in.close();
            out.close();

            if (widgetPreviewFile.exists()) {
                ZipFile zipFile = new ZipFile(widgetPreviewFile);
                Enumeration<? extends ZipEntry> entryEnum = zipFile.entries();
                ZipEntry entry = entryEnum.nextElement();
                while (entry != null) {
                    if (!entry.getName().contains("/") && entry.getName().contains("thumb")) {
                        if (entry.getName().contains(thumbNames[0])) {
                            InputStream zipIn = null;
                            OutputStream zipOut = null;
                            try {
                                zipIn = zipFile.getInputStream(entry);
                                zipOut = new FileOutputStream(preview1);
                                Utils.copyFiles(zipIn, zipOut);
                            } finally {
                                if (zipIn != null) zipIn.close();
                                if (zipOut != null) zipOut.close();
                            }
                        } else if (thumbNames[1] != null && !(thumbNames[1].isEmpty())) {
                            if (entry.getName().contains(thumbNames[1])) {
                                InputStream zipIn = null;
                                OutputStream zipOut = null;
                                try {
                                    zipIn = zipFile.getInputStream(entry);
                                    zipOut = new FileOutputStream(preview2);
                                    Utils.copyFiles(zipIn, zipOut);
                                } finally {
                                    if (zipIn != null) zipIn.close();
                                    if (zipOut != null) zipOut.close();
                                }
                            }
                        }
                    }
                    try {
                        if (entryEnum.hasMoreElements())
                            entry = entryEnum.nextElement();
                        else entry = null;
                    } catch (Exception e) {
                        entry = null;
                        Log.e("Kustom", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Kustom", e.getMessage());
        }
        return new String[]{preview1.getAbsolutePath(), preview2.getAbsolutePath()};
    }

}