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
			InitializeComponent();
		}

        private  void OnButtonClicked(object sender, EventArgs args)
        {
            _counter.inc();
            label.Text = _counter.getCounter() + "x clicked";
        }

        private void Test()
        {
            
        }
	}
}
