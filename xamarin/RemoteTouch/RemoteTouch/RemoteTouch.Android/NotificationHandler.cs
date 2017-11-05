using Android.Content;
using Android.OS;
using Android.Service.Notification;
using Android.Util;

namespace RemoteTouch.Droid
{
    public class NotificationHandler : NotificationListenerService
    {
        private Context _context;

        public Context Context { get => _context; set => _context = value; }

        public override void OnCreate()
        {
            base.OnCreate();
            Context = base.ApplicationContext;
            System.Console.WriteLine("Start NotificationHandler");
        }

        public override void OnNotificationPosted(StatusBarNotification sbn)
        {
            string pack = sbn.PackageName;
            string ticker = sbn.Notification.TickerText.ToString();
            Bundle extras = sbn.Notification.Extras;
            string title = extras.GetString("android.title");
            string text = extras.GetCharSequence("android.text").ToString();

            Log.Info("Package", pack);
            Log.Info("Ticker", ticker);
            Log.Info("Title", title);
            Log.Info("Text", text);

            System.Console.WriteLine("Package: "+ pack);
            System.Console.WriteLine("Ticker: "+ ticker);
            System.Console.WriteLine("Title: "+ title);
            System.Console.WriteLine("Text: "+ text);

            // Create Intent for BroadcastReceiver(s)
            //Intent msgrcv = new Intent("Msg");
            //msgrcv.PutExtra("package", pack);
            //msgrcv.PutExtra("ticker", ticker);
            //msgrcv.PutExtra("title", title);
            //msgrcv.PutExtra("text", text);

            // Send broadcast
            //LocalBroadcastManager.getInstance(Context).sendBroadcast(msgrcv);
        }

        public override void OnNotificationRemoved(StatusBarNotification sbn)
        {
            Log.Info("Msg", "Notification Removed");
            System.Console.WriteLine("Notification Removed");
        }
    }
}
