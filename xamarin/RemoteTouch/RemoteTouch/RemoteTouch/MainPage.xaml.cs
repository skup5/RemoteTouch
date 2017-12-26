using System;
using Xamarin.Forms;

namespace RemoteTouch
{
    public partial class MainPage : ContentPage
    {
        private int count = 0;

        public MainPage()
        {
            InitializeComponent();
        }

        private void OnButtonClicked(object sender, EventArgs args)
        {
            count++;
            label.Text = count + "x clicked";
        }
    }
}
