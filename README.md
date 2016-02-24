Multimedia Search Engine
================


A Video Retrieve Application based on Java, with image, audio, and motion retrieve components


Key features
-------------

###Image Retrieve
* Color Histogram (Using Jensen-Shannon divergence for clip matching)
* SURF (we use JopenSurf to extract the surf information, record the number of interest points of every frame)
* Contrast (Traverse every pixel, extract gray-scale of them. Calculate the contrast of the frame with formula provided by Marko Keuschig)

###Audio Retrieve
* FFT (Apply a window to the data - size of 1024 and discard exceeded bits. Transform the data using FFT. Find the peak value in the transformed data)


###Motion Retrieve
* Compute motion vectors (8*8 Macroblock, Logarithmic Search k = 16)
* Compute a motion value of each frame
* Compare databases and the query frame by frame

Contributors
------------
Xiaoting Cai, Yu-Tung Lee, Huaying Tsai
