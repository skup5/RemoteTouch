using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
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

        private  void OnButtonClicked(object sender, EventArgs args)
        {
            count++;
            label.Text = count + "x clicked";
        }
	}
}
