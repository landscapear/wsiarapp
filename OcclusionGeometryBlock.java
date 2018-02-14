/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.vuforia.samples.FloodAR;

import java.nio.Buffer;

import android.util.Log;


public class OcclusionGeometryBlock extends MeshObject
{
	
    // Data for drawing the 3D plane as overlay
    private double [] occgVertices;
    
    private static final double occgTexcoords[] = { 
            0, 0, 1, 0, 1, 1, 0, 1,			// faces
                                                
            1, 0, 0, 0, 0, 1, 1, 1,
                                                
            0, 0, 1, 0, 1, 1, 0, 1,
                                                
            1, 0, 0, 0, 0, 1, 1, 1,
                                                
            0, 0, 1, 0, 1, 1, 0, 1,
                                                
            1, 0, 0, 0, 0, 1, 1, 1 };
    
    
    private static final double occgNormals[]   = { 
            0, 0, 1,  0, 0, 1,  0, 0, 1,  0, 0, 1,
            
            0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,
            
            -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
            
            1, 0, 0,  1, 0, 0,  1, 0, 0,  1, 0, 0,
            
            0, 1, 0,  0, 1, 0,  0, 1, 0,  0, 1, 0,
            
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,
            };
    
    private static final short  occgIndices[]   = {
            0, 1, 2, 0, 2, 3, // front
            4, 6, 5, 4, 7, 6, // back
            8, 9, 10, 8, 10, 11, // left
            12, 14, 13, 12, 15, 14, // right
            16, 17, 18, 16, 18, 19, // top
            20, 22, 21, 20, 23, 22  // bottom
                                                };
    
    private Buffer mVertBuff;
    private Buffer mTexCoordBuff;
    private Buffer mNormBuff;
    private Buffer mIndBuff;

    
    // call this after parameter changes and before a draw call
    public double [] reconstructVertices()
    {
    	occgVertices = new double [] {
        		v1[0], v1[1]-bot, v1[2]+front, 			// front
                v2[0], v2[1]-bot, v2[2]+front, 
                v2[0], v2[1]+top, v2[2]+front,
                v1[0], v1[1]+top, v1[2]+front,

        		v1[0], v1[1]-bot, v1[2]-back, 		// back
                v2[0], v2[1]-bot, v2[2]-back, 
                v2[0], v2[1]+top, v2[2]-back,
                v1[0], v1[1]+top, v1[2]-back,

                v1[0], v1[1]-bot, v1[2]-back, 		// left
                v1[0], v1[1]-bot, v1[2]+front,
                v1[0], v1[1]+top, v1[2]+front,
                v1[0], v1[1]+top, v1[2]-back,

                v2[0], v2[1]-bot, v2[2]-back, 		// right
                v2[0], v2[1]-bot, v2[2]+front,
                v2[0], v2[1]+top, v2[2]+front,
                v2[0], v2[1]+top, v2[2]-back,

                v1[0], v1[1]+top, v1[2]+front,			// top
                v2[0], v2[1]+top, v2[2]+front,
                v2[0], v2[1]+top, v2[2]-back,
                v1[0], v1[1]+top, v1[2]-back,

                v1[0], v1[1]-bot, v1[2]+front,			// bottom
                v2[0], v2[1]-bot, v2[2]+front,
                v2[0], v2[1]-bot, v2[2]-back,
                v1[0], v1[1]-bot, v1[2]-back };
        
    	return occgVertices;
    }

    public OcclusionGeometryBlock(double [] vert1, double [] vert2)
    {
    	this(vert1, vert2, 60, 0, 120, 0, 0);
    }

    public OcclusionGeometryBlock(double [] vert1, double [] vert2, double bot, double top, double back, double degrees, double front)
    {
    	this.bot = bot;			// these values should always be positive
    	this.top = top;
    	this.back = back;
    	this.degrees = degrees;	// swing angle from the pinch corner (in degrees)
    	this.front = front;

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
