/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.vuforia.samples.FloodAR;

import java.nio.Buffer;
import android.util.Log;

public class OcclusionGeometryArch extends MeshObject
{	
    // Data for drawing the 3D plane as overlay
    private double [] occgVertices;

	int n = 15;
	double [] cx = new double[n];
	double [] cy = new double[n];
	double [] cz = new double[n];
	double [] ty = new double[n];
	double angle, anglestep;
	double [] lv = new double[3];
	double len, r, px, py, pz;
	double cosa, sina, u, v, w, lenuvw, x, y, z, temp;

    private static final double occgTexcoords[] = { 
            0, 0, 1, 0, 1, 1, 0, 1,			
            0, 0, 1, 0, 1, 1, 0, 1,			
            0, 0, 1, 0, 1, 1, 0, 1,			
            0, 0, 1, 0, 1, 1, 0, 1,			
            0, 0, 1, 0, 1, 1, 0, 1,			
            0, 0, 1, 0, 1, 1, 0, 1,			
            0, 0, 1, 0, 1, 1, 0, 1,			
            0, 0, 1, 0, 1, 1, 0, 1,			

            0, 0, 1, 0, 1, 1, 0, 1,			
            0, 0, 1, 0, 1, 1, 0, 1,			
            0, 0, 1, 0, 1, 1, 0, 1,	

            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	

            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	

            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
            0, 0, 1, 0, 1, 1, 0, 1,	
};
    
    
    private static final double occgNormals[]   = {
    		// front
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            
            // left
            -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, 
            
            // right
            1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,              

            // top
            0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 
            
            // bottom
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            
            // arch
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,

            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,

            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,

            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,
            
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  

            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  

            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  

            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  
    };
    
    // note - we need to duplicate vertices so that the normals match for each different
    // vertex... may take some time to do this... just do easiest faces first, then finish
    // off arch last... should be ok really
    
    private static final short  occgIndices[]   = {
    		// front
    		0, 3, 2,
    		0, 2, 1,
    		4, 7, 6,
    		4, 6, 5,
    		
    		// left
    		8, 9, 10,
    		8, 10, 11,
    		
    		// right
    		12, 14, 13,
    		12, 15, 14,
    		
    		// top
    		16, 17, 18,
    		16, 18, 19,
    		
    		// bottom
    		20, 23, 22,
    		20, 22, 21,
    		24, 27, 26,
    		24, 26, 25,
    		
    		// inside left
    		28, 30, 29,
    		28, 31, 30,
    		
    		// inside right
    		32, 33, 34,
    		32, 34, 35,
    		
    		// arch front
    		36, 37, 38,
    		38, 39, 36,
    		
    		40, 41, 42,
    		42, 43, 40,
    		
    		44, 45, 46,
    		46, 47, 44,
    		
    		48, 49, 50,
    		50, 51, 48,

    		52, 53, 54,
    		54, 55, 52,

    		56, 57, 58,
    		58, 59, 56,

    		60, 61, 62,
    		62, 63, 60,

    		64, 65, 66,
    		66, 67, 64,

    		68, 69, 70,
    		70, 71, 68,
    		
    		72, 73, 74,
    		74, 75, 72,
    		
    		76, 77, 78,
    		78, 79, 76,
    		
    		80, 81, 82,
    		82, 83, 80,
    		
    		84, 85, 86,
    		86, 87, 84,
    		
    		88, 89, 90,
    		90, 91, 88,
    		
    		// arch underneath
    		39, 92, 36,
    		39, 93, 92,
    		
       		43, 94, 40,
    		43, 95, 94,

       		47, 96, 44,
    		47, 97, 96,

       		51, 98, 48,
    		51, 99, 98,

       		55, 100, 52,
    		55, 101, 100,

       		59, 102, 56,
    		59, 103, 102,

       		63, 104, 60,
    		63, 105, 104,

       		67, 106, 64,
    		67, 107, 106,

       		71, 108, 68,
    		71, 109, 108,

       		75, 110, 72,
    		75, 111, 110,
    
       		79, 112, 76,
    		79, 113, 112,

       		83, 114, 80,
    		83, 115, 114,

       		87, 116, 84,
    		87, 117, 116,

       		91, 118, 88,
    		91, 119, 118,
    };
    
    private Buffer mVertBuff;
    private Buffer mTexCoordBuff;
    private Buffer mNormBuff;
    private Buffer mIndBuff;
    
    // call this after parameter changes and before a draw call
    public double [] reconstructVertices()
    {
    	// create arch
    	angle = 0;
    	anglestep = 180.0 / (n-1);
    	
    	lv[0] = v2[0]-v1[0];
    	lv[1] = v2[1]-v1[1];
    	lv[2] = v2[2]-v1[2];
    	
    	len = Math.sqrt(lv[0]*lv[0] + lv[1]*lv[1] + lv[2]*lv[2]);

    	lv[0] /= len;
    	lv[1] /= len;
    	lv[2] /= len;

    	r = len/2 - archwidth;

    	// centre point
    	px = v1[0] + lv[0]*len/2;
    	py = v1[1] + lv[1]*len/2 - bridgetopheight - archeight;
    	pz = v1[2] + lv[2]*len/2;
    	
    	// point to rotate
    	x = lv[0]*r;
    	y = lv[1]*r;
    	z = lv[2]*r;
    	
    	// normal about which to rotate
    	u = lv[2];
    	v = 0;
    	w = -lv[0];
    	lenuvw = Math.sqrt(u*u + v*v + w*w);
    	u /= lenuvw;
    	v /= lenuvw;
    	w /= lenuvw;

    	for(int c1 = 0; c1 < n; c1++)
    	{
    		// work out point
    		cosa = Math.cos(Math.toRadians(angle));
    		sina = Math.sin(Math.toRadians(angle));
    		temp = u*x + v*y + w*z;

    		// computed point
    		cx[c1] = u*temp*(1-cosa) + x*cosa + (-w*y + v*z) * sina;
    		cy[c1] = v*temp*(1-cosa) + y*cosa + ( w*x - u*z) * sina;
    		cz[c1] = w*temp*(1-cosa) + z*cosa + (-v*x + u*y) * sina;
    		
    		cx[c1] += px;
    		cy[c1] += py;
    		cz[c1] += pz;
    		
    		ty[c1] = v2[1] - lv[1] * 2 * r * c1 / (n-1);
    		
    		angle -= anglestep;
    	}
    	    	
    	// create data
    	
    	occgVertices = new double [] {
    			// *** front face
    			
    			v1[0], v1[1], v1[2],
    			v1[0]+ archwidth *lv[0], v1[1]+ archwidth *lv[1], v1[2]+ archwidth *lv[2],
    			v1[0]+ archwidth *lv[0], v1[1]+ archwidth *lv[1]-bot, v1[2]+ archwidth *lv[2],
    			v1[0], v1[1]-bot, v1[2],

    			v2[0]- archwidth *lv[0], v2[1]- archwidth *lv[1], v2[2]- archwidth *lv[2],
    			v2[0], v2[1], v2[2],
    			v2[0], v2[1]-bot, v2[2],
    			v2[0]- archwidth *lv[0], v2[1]- archwidth *lv[1]-bot, v2[2]- archwidth *lv[2],
    			
    			// left
    			
    			v1[0], v1[1], v1[2],
    			v1[0], v1[1], v1[2]-back,
    			v1[0], v1[1]-bot, v1[2]-back,
    			v1[0], v1[1]-bot, v1[2],
    			
    			// right

    			v2[0], v2[1], v2[2],
    			v2[0], v2[1], v2[2]-back,
    			v2[0], v2[1]-bot, v2[2]-back,
    			v2[0], v2[1]-bot, v2[2],
    			
    			// top
    			v1[0], v1[1], v1[2],
    			v2[0], v2[1], v2[2],
    			v2[0], v2[1], v2[2]-back,
    			v1[0], v1[1], v1[2]-back,
    			
    			// bottom
    			v1[0], v1[1]-bot, v1[2],
    			v1[0]+ archwidth *lv[0], v1[1]+ archwidth *lv[1]-bot, v1[2]+ archwidth *lv[2],
    			v1[0]+ archwidth *lv[0], v1[1]+ archwidth *lv[1]-bot, v1[2]+ archwidth *lv[2]-back,
    			v1[0], v1[1]-bot, v1[2]-back,
    			
    			v2[0]- archwidth *lv[0], v2[1]- archwidth *lv[1]-bot, v2[2]- archwidth *lv[2],
    			v2[0], v2[1]-bot, v2[2],
    			v2[0], v2[1]-bot, v2[2]-back,
    			v2[0]- archwidth *lv[0], v2[1]- archwidth *lv[1]-bot, v2[2]- archwidth *lv[2]-back,

    			// inside left
    			v1[0]+ archwidth *lv[0], v1[1]+ archwidth *lv[1], v1[2]+ archwidth *lv[2],
    			v1[0]+ archwidth *lv[0], v1[1]+ archwidth *lv[1], v1[2]+ archwidth *lv[2]-back,
    			v1[0]+ archwidth *lv[0], v1[1]+ archwidth *lv[1]-bot, v1[2]+ archwidth *lv[2]-back,
    			v1[0]+ archwidth *lv[0], v1[1]+ archwidth *lv[1]-bot, v1[2]+ archwidth *lv[2],
    			
    			// inside right
    			v2[0]- archwidth *lv[0], v2[1]- archwidth *lv[1], v2[2]- archwidth *lv[2],
    			v2[0]- archwidth *lv[0], v2[1]- archwidth *lv[1], v2[2]- archwidth *lv[2]-back,
    			v2[0]- archwidth *lv[0], v2[1]- archwidth *lv[1]-bot, v2[2]- archwidth *lv[2]-back,
    			v2[0]- archwidth *lv[0], v2[1]- archwidth *lv[1]-bot, v2[2]- archwidth *lv[2],

    			// front arch
    			cx[0], cy[0], cz[0],
    			cx[0], ty[0], cz[0],
    			cx[1], ty[1], cz[1],
    			cx[1], cy[1], cz[1],

    			cx[1], cy[1], cz[1],
    			cx[1], ty[1], cz[1],
    			cx[2], ty[2], cz[2],
    			cx[2], cy[2], cz[2],

    			cx[2], cy[2], cz[2],
    			cx[2], ty[2], cz[2],
    			cx[3], ty[3], cz[3],
    			cx[3], cy[3], cz[3],

    			cx[3], cy[3], cz[3],
    			cx[3], ty[3], cz[3],
    			cx[4], ty[4], cz[4],
    			cx[4], cy[4], cz[4],

    	
    			cx[4], cy[4], cz[4],
    			cx[4], ty[4], cz[4],
    			cx[5], ty[5], cz[5],
    			cx[5], cy[5], cz[5],

    			cx[5], cy[5], cz[5],
    			cx[5], ty[5], cz[5],
    			cx[6], ty[6], cz[6],
    			cx[6], cy[6], cz[6],

    			cx[6], cy[6], cz[6],
    			cx[6], ty[6], cz[6],
    			cx[7], ty[7], cz[7],
    			cx[7], cy[7], cz[7],

    			cx[7], cy[7], cz[7],
    			cx[7], ty[7], cz[7],
    			cx[8], ty[8], cz[8],
    			cx[8], cy[8], cz[8],

    			cx[8], cy[8], cz[8],
    			cx[8], ty[8], cz[8],
    			cx[9], ty[9], cz[9],
    			cx[9], cy[9], cz[9],

    			cx[9], cy[9], cz[9],
    			cx[9], ty[9], cz[9],
    			cx[10], ty[10], cz[10],
    			cx[10], cy[10], cz[10],

    			cx[10], cy[10], cz[10],
    			cx[10], ty[10], cz[10],
    			cx[11], ty[11], cz[11],
    			cx[11], cy[11], cz[11],

    			cx[11], cy[11], cz[11],
    			cx[11], ty[11], cz[11],
    			cx[12], ty[12], cz[12],
    			cx[12], cy[12], cz[12],

    			cx[12], cy[12], cz[12],
    			cx[12], ty[12], cz[12],
    			cx[13], ty[13], cz[13],
    			cx[13], cy[13], cz[13],

    			cx[13], cy[13], cz[13],
    			cx[13], ty[13], cz[13],
    			cx[14], ty[14], cz[14],
    			cx[14], cy[14], cz[14],

    			// back arch
    			cx[0], cy[0], cz[0]-back,
    			cx[1], cy[1], cz[1]-back,

    			cx[1], cy[1], cz[1]-back,
    			cx[2], cy[2], cz[2]-back,

    			cx[2], cy[2], cz[2]-back,
    			cx[3], cy[3], cz[3]-back,

    			cx[3], cy[3], cz[3]-back,
    			cx[4], cy[4], cz[4]-back,
    			
    			cx[4], cy[4], cz[4]-back,
    			cx[5], cy[5], cz[5]-back,

    			cx[5], cy[5], cz[5]-back,
    			cx[6], cy[6], cz[6]-back,

    			cx[6], cy[6], cz[6]-back,
    			cx[7], cy[7], cz[7]-back,

    			cx[7], cy[7], cz[7]-back,
    			cx[8], cy[8], cz[8]-back,

    			cx[8], cy[8], cz[8]-back,
    			cx[9], cy[9], cz[9]-back,

    			cx[9], cy[9], cz[9]-back,
    			cx[10], cy[10], cz[10]-back,

    			cx[10], cy[10], cz[10]-back,
    			cx[11], cy[11], cz[11]-back,

    			cx[11], cy[11], cz[11]-back,
    			cx[12], cy[12], cz[12]-back,

    			cx[12], cy[12], cz[12]-back,
    			cx[13], cy[13], cz[13]-back,

    			cx[13], cy[13], cz[13]-back,
    			cx[14], cy[14], cz[14]-back,
    		};

//    	occgVertices = new double [] {
//    			// *** front face
//    			
//    			v1[0], v1[1], v1[2],
//    			v1[0]+archwidth, v1[1], v1[2],
//    			v1[0]+archwidth, v1[1]-bot, v1[2],
//    			v1[0], v1[1]-bot, v1[2],
//
//    			v2[0]-archwidth, v2[1], v2[2],
//    			v2[0], v2[1], v2[2],
//    			v2[0], v2[1]-bot, v2[2],
//    			v2[0]-archwidth, v2[1]-bot, v2[2],
//    			
//    			// left
//    			
//    			v1[0], v1[1], v1[2],
//    			v1[0], v1[1], v1[2]-back,
//    			v1[0], v1[1]-bot, v1[2]-back,
//    			v1[0], v1[1]-bot, v1[2],
//    			
//    			// right
//
//    			v2[0], v2[1], v2[2],
//    			v2[0], v2[1], v2[2]-back,
//    			v2[0], v2[1]-bot, v2[2]-back,
//    			v2[0], v2[1]-bot, v2[2],
//    			
//    			// top
//    			v1[0], v1[1], v1[2],
//    			v2[0], v2[1], v2[2],
//    			v2[0], v2[1], v2[2]-back,
//    			v1[0], v1[1], v1[2]-back,
//    			
//    			// bottom
//    			v1[0], v1[1]-bot, v1[2],
//    			v1[0]+archwidth, v1[1]-bot, v1[2],
//    			v1[0]+archwidth, v1[1]-bot, v1[2]-back,
//    			v1[0], v1[1]-bot, v1[2]-back,
//    			
//    			v2[0]-archwidth, v2[1]-bot, v2[2],
//    			v2[0], v2[1]-bot, v2[2],
//    			v2[0], v2[1]-bot, v2[2]-back,
//    			v2[0]-archwidth, v2[1]-bot, v2[2]-back,
//
//    			// inside left
//    			v1[0]+archwidth, v1[1], v1[2],
//    			v1[0]+archwidth, v1[1], v1[2]-back,
//    			v1[0]+archwidth, v1[1]-bot, v1[2]-back,
//    			v1[0]+archwidth, v1[1]-bot, v1[2],
//    			
//    			// inside right
//    			v2[0]-archwidth, v2[1], v2[2],
//    			v2[0]-archwidth, v2[1], v2[2]-back,
//    			v2[0]-archwidth, v2[1]-bot, v2[2]-back,
//    			v2[0]-archwidth, v2[1]-bot, v2[2],
//
//    			// arch
//    			cx[0], cy[0], v2[2],
//    			cx[0], v2[1], v2[2],
//    			cx[1], v2[1], v2[2],
//    			cx[1], cy[1], v2[2],
//
//    			cx[1], cy[1], v2[2],
//    			cx[1], v2[1], v2[2],
//    			cx[2], v2[1], v2[2],
//    			cx[2], cy[2], v2[2],
//
//    			cx[2], cy[2], v2[2],
//    			cx[2], v2[1], v2[2],
//    			cx[3], v2[1], v2[2],
//    			cx[3], cy[3], v2[2],
//
//    			cx[3], cy[3], v2[2],
//    			cx[3], v2[1], v2[2],
//    			cx[4], v2[1], v2[2],
//    			cx[4], cy[4], v2[2],
//    	};

    	return occgVertices;
    }

    public OcclusionGeometryArch(double [] vert1, double [] vert2)
    {
    	this(vert1, vert2, 60, 60, 5, 8, 8);
    }

    public OcclusionGeometryArch(double [] vert1, double [] vert2, double bot, double back, double bridgecolumnwidth, double bridgetopheight, double bridgearcheight)
    {
    	this.bot = bot;			// these values should always be positive
    	this.top = 0;
    	this.back = back;
    	this.archwidth = bridgecolumnwidth;
    	this.bridgetopheight = bridgetopheight;
    	this.archeight = bridgearcheight;
    	this.degrees = 0;	// swing angle from the pinch corner (in degrees)

    	mTexCoordBuff = fillBuffer(occgTexcoords);
        mNormBuff = fillBuffer(occgNormals);
        mIndBuff = fillBuffer(occgIndices);
        
        v1 = new double [] {vert1[0], vert1[1], vert1[2]};
        v2 = new double [] {vert2[0], vert2[1], vert2[2]};

        fillVertexBuffer();
        
        Log.v("MYINFO", "occgeom vert (" + v1[0] + ", " + v1[1] + ", " + v1[2] + ") added.");
    }
    
    public void fillVertexBuffer()
    {
    	mVertBuff = fillBuffer(reconstructVertices());
    }
    
    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = mVertBuff;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = mTexCoordBuff;
                break;
            case BUFFER_TYPE_INDICES:
                result = mIndBuff;
                break;
            case BUFFER_TYPE_NORMALS:
                result = mNormBuff;
            default:
                break;
        }
        return result;
    }
    
    
    @Override
    public int getNumObjectVertex()
    {
        return occgVertices.length / 3;
    }
    
    
    @Override
    public int getNumObjectIndex()
    {
        return occgIndices.length;
    }
}
