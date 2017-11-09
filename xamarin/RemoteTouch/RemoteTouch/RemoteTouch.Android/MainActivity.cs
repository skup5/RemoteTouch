using System;
using System.Text;
using Android.App;
using Android.Content.PM;
using Android.Widget;
using Android.OS;
using Android.Util;
using Android.Content;
using Android.Provider;
using Android.Support.V4.Content;
using Android.Text;

namespace RemoteTouch.Droid
{
    [Activity(Label = "@string/AppName", Icon = "@drawable/icon", Theme = "@style/MainTheme", MainLauncher = true,
        ConfigurationChanges = ConfigChanges.ScreenSize | ConfigChanges.Orientation)]
    public class MainActivity : Xamarin.Forms.Platform.Android.FormsAppCompatActivity
    {
        private const string ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
        private NotificationHandlerReceiver _notificationHandlerReceiver;

        protected override void OnCreate(Bundle bundle)
        {
            TabLayoutResource = Resource.Layout.Tabbar;
            ToolbarResource = Resource.Layout.Toolbar;

            base.OnCreate(bundle);
            Xamarin.Forms.Forms.Init(this, bundle);
            LoadApplication(new RemoteTouch.App());

            Log.Info("Msg", "Start MainActivity");
            Console.WriteLine("Start MainActivity");

            _notificationHandlerReceiver = new NotificationHandlerReceiver();
            LocalBroadcastManager.GetInstance(this).RegisterReceiver(_notificationHandlerReceiver,
                new IntentFilter("cz.zelenikr.remotetouch.notificationhandler"));

            EnableNotificationService();
        }


        private bool IsNotificationServiceEnabled()
        {
            String pkgName = PackageName;
            string flat = Settings.Secure.GetString(ContentResolver,
                ENABLED_NOTIFICATION_LISTENERS);
            if (!TextUtils.IsEmpty(flat))
            {
                string[] names = flat.Split(':');
                for (int i = 0; i < names.Length; i++)
                {
                    ComponentName cn = ComponentName.UnflattenFromString(names[i]);
                    if (cn != null)
                    {
                        if (TextUtils.Equals(pkgName, cn.PackageName))
                        {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private void EnableNotificationService()
        {
            if (!IsNotificationServiceEnabled())
            {
                new Android.Support.V7.App.AlertDialog.Builder(this)
                    .SetIcon(Resource.Drawable.Icon)
                    .SetTitle(Resource.String.AppName)
                    .SetMessage("Je nutné povolit oprávnění. Přejete si otevřít příslušné nastavení?")
                    .SetPositiveButton("Ano",
                        (sender, args) =>
                            StartActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")))
                    .SetNegativeButton("Ne", (sender, args) => { })
                    .Show();
            }
        }
    }


    class NotificationHandlerReceiver : BroadcastReceiver
    {
        public override void OnReceive(Context context, Intent intent)
        {
            Log.Info(GetType().Name, "OnReceive()");
            Console.WriteLine("NotificationHandlerReceiver.OnReceive()");

            StringBuilder msg = new StringBuilder("NotificationHandler:\n");
            msg.AppendLine(intent.GetStringExtra("package"))
                .AppendLine(intent.GetStringExtra("ticker"))
                .AppendLine(intent.GetStringExtra("title"))
                .AppendLine(intent.GetStringExtra("text"));

            Toast.MakeText(context, msg.ToString(), ToastLength.Long).Show();
        }
    }
}