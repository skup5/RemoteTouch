using Android.App;
using Android.Content;
using Android.OS;
using Android.Service.Notification;
using Android.Support.V4.Content;
using Android.Util;

namespace RemoteTouch.Droid
{
    [Service(Label = "RemoteTouch", Permission = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE")]
    [IntentFilter(new[] {"android.service.notification.NotificationListenerService"})]
    public class NotificationHandler : NotificationListenerService
    {
        private Context _context;

        public Context Context
        {
            get => _context;
            set => _context = value;
        }

        public override void OnCreate()
        {
            base.OnCreate();
            Context = ApplicationContext;
            System.Console.WriteLine("Start NotificationHandler");
        }

        public override void OnNotificationPosted(StatusBarNotification sbn)
        {
            string pack = sbn.PackageName;
            string ticker = sbn.Notification.TickerText != null ? sbn.Notification.TickerText.ToString() : "null";
            Bundle extras = sbn.Notification.Extras;
            string title = extras.GetString("android.title");
            string text = extras.GetCharSequence("android.text");

            Log.Info("Package", pack);
            Log.Info("Ticker", ticker);
            Log.Info("Title", title);
            Log.Info("Text", text);

            System.Console.WriteLine("Package: " + pack);
            System.Console.WriteLine("Ticker: " + ticker);
            System.Console.WriteLine("Title: " + title);
            System.Console.WriteLine("Text: " + text);

            // Create Intent for BroadcastReceiver(s)
            Intent msgrcv = new Intent("cz.zelenikr.remotetouch.notificationhandler");
            msgrcv.PutExtra("package", pack);
            msgrcv.PutExtra("ticker", ticker);
            msgrcv.PutExtra("title", title);
            msgrcv.PutExtra("text", text);

            // Send broadcast
            LocalBroadcastManager.GetInstance(Context).SendBroadcast(msgrcv);
        }

        public override void OnNotificationRemoved(StatusBarNotification sbn)
        {
            Log.Info(GetType().Name, "Notification Removed");
            System.Console.WriteLine("Notification Removed");
        }
    }
}