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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.activities.base.DrawerActivity;
import jahirfiquitiva.iconshowcase.fragments.MainFragment;
import jahirfiquitiva.iconshowcase.holders.lists.FullListHolder;
import jahirfiquitiva.iconshowcase.models.ZooperWidget;
import jahirfiquitiva.iconshowcase.utilities.utils.Utils;
import timber.log.Timber;

public class LoadZooperWidgets extends AsyncTask<Void, String, Boolean> {

    private final ArrayList<ZooperWidget> widgets = new ArrayList<>();
    private final WeakReference<Context> context;

    public LoadZooperWidgets(Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected Boolean doInBackground(Void... params) {
        boolean worked = false;
        try {
            AssetManager assetManager = context.get().getAssets();
            String[] templates = assetManager.list("templates");

            File previewsFolder = new File(context.get().getExternalCacheDir(),
                    "ZooperWidgetsPreviews");

            if (templates != null && templates.length > 0) {
                Utils.clean(previewsFolder);
                previewsFolder.mkdirs();
                for (String template : templates) {
                    File widgetPreviewFile = new File(previewsFolder, template);
                    String widgetName = Utils.getFilenameWithoutExtension(template);
                    String preview = getWidgetPreviewPathFromZip(context, widgetName,
                            assetManager.open("templates/" + template), previewsFolder,
                            widgetPreviewFile);
                    widgets.add(new ZooperWidget(preview));
                    widgetPreviewFile.delete();
                }
                worked = widgets.size() == templates.length;
            }
        } catch (Exception e) {
            worked = false;
        }
        return worked;
    }

    @Override
    protected void onPostExecute(Boolean worked) {
        if (worked) {
            FullListHolder.get().zooperList().createList(widgets);
            if (context.get() instanceof ShowcaseActivity) {
                if (((ShowcaseActivity) context.get()).getCurrentFragment() instanceof
                        MainFragment) {
                    ((MainFragment) ((ShowcaseActivity) context.get()).getCurrentFragment())
                            .updateAppInfoData();
                }
                ((ShowcaseActivity) context.get()).resetFragment(DrawerActivity.DrawerItem.ZOOPER);
            }
        } else {
            Timber.e("Something went really wrong while loading zooper widgets.");
        }

    }

    /**
     * This code was created by Aidan Follestad. Complete credits to him.
     */
    @SuppressWarnings("ThrowFromFinallyBlock")
    private String getWidgetPreviewPathFromZip(WeakReference<Context> context, String name,
                                               InputStream in,
                                               File previewsFolder, File widgetPreviewFile) {
        OutputStream out;
        File preview = new File(previewsFolder, name + ".png");

        try {
            out = new FileOutputStream(widgetPreviewFile);
            Utils.copyFiles(in, out);
            in.close();
            out.close();

            if (widgetPreviewFile.exists()) {
                ZipFile zipFile = new ZipFile(widgetPreviewFile);
                Enumeration<? extends ZipEntry> entryEnum = zipFile.entries();
                ZipEntry entry;
                while ((entry = entryEnum.nextElement()) != null) {
                    if (entry.getName().endsWith("screen.png")) {
                        InputStream zipIn = null;
                        OutputStream zipOut = null;
                        try {
                            zipIn = zipFile.getInputStream(entry);
                            zipOut = new FileOutputStream(preview);
                            Utils.copyFiles(zipIn, zipOut);
                        } finally {
                            if (zipIn != null) zipIn.close();
                            if (zipOut != null) zipOut.close();
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            //Do nothing
        }

        if (context.get().getResources().getBoolean(R.bool.remove_zooper_previews_background)) {
            out = null;
            try {
                Bitmap bmp = ZooperWidget.getTransparentBackgroundPreview(
                        BitmapFactory.decodeFile(preview.getAbsolutePath()));
                out = new FileOutputStream(preview);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                Timber.e("ZooperIOException", e.getLocalizedMessage());
            } finally {
                try {
                    if (out != null) out.close();
                } catch (IOException e1) {
                    Timber.e("ZooperIOException2", e1.getLocalizedMessage());
                }
            }
        }

        return preview.getAbsolutePath();
    }

}