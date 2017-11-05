using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Xamarin.Forms;
using Plugin;
using CrossLibrary;

namespace RemoteTouch
{
    public partial class MainPage : ContentPage
    {
        private MyCounter _counter;

        public MainPage()
        {
            _counter= new MyCounter();
            InitializeComponent();
        }

        private void OnButtonClicked(object sender, EventArgs args)
        {
            _counter.inc();
            label.Text = _counter.getCounter() + "x clicked";
            Console.WriteLine("[*] Counter clicked");
        }

        private void OnCreateNotification(object sender, EventArgs args)
        {
            Plugin.LocalNotifications.CrossLocalNotifications.Current.Show("RemoteTouch", label.Text, (int)_counter.getCounter());
            Console.WriteLine("[*] Create notification");
        }

        private void Test()
        {

        }
    }
}
