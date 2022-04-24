package xyz.webtubeapp

import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import android.app.PendingIntent

import android.content.ComponentName
import android.content.pm.PackageInstaller.SessionParams
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.ContextCompat.startActivity








class utils() {
    private val REQUEST_DELETE_PACKAGES: Int = 1

    private fun Context.isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            //Toast.makeText(this, "erreur $e", Toast.LENGTH_LONG).show()
            false
        }
    }
    fun askToUninstallOldApk(ctx : Context){

        if (ctx.isPackageInstalled("dev.androne.webtube")) {


            val alertDialog: AlertDialog? = ctx.let {
                val builder = AlertDialog.Builder(ctx)
                builder.apply {
                    setPositiveButton(R.string.ok
                    ) { _, _ ->
                        Toast.makeText(ctx, ctx.getString(R.string.manually_uninstall_old_package), Toast.LENGTH_LONG).show()
                        val packageURI = Uri.parse("package:" + "dev.androne.webtube")
                        val uninstallIntent = Intent(Intent.ACTION_DELETE, packageURI)
                        it.startActivity(uninstallIntent)
                    }
                    setNegativeButton(R.string.cancel
                    ) { _, _ ->
                    }
                }
                builder.setMessage(R.string.dialog_message_uninstall_old_package)
                    .setTitle(R.string.dialog_title_uninstall_old_package)

                builder.create()
            }
            alertDialog?.show()



        }

    }




}