using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Xamarin.Forms;

namespace CrossLibrary
{
    public class CrossLibrary
    {
        public CrossLibrary()
        {
        }
    }

    public class MyCounter
    {
        private long _counter;

        public void inc()
        {
            _counter++;
        }

        public long getCounter()
        {
            return _counter;
        }
    }
}