/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.vuforia.samples.FloodAR;

import java.io.Serializable;
import java.nio.Buffer;
import java.util.ArrayList;


public class SphereObject extends MeshObject implements Serializable
{
    // Data for drawing the 3D plane as overlay
    private static transient double [] sphereVertices;    
    private static transient double [] sphereTexcoords;
    private static transient double [] sphereNormals;
    private static short [] sphereIndices;
    
    private transient Buffer mVertBuff;
    private transient Buffer mTexCoordBuff;
    private transient Buffer mNormBuff;
    private transient Buffer mIndBuff;
    
    public SphereObject()
    {
    	super();

    	// inefficient but quick sphere data
    	
    	int num_points = 16;
    	double as = Math.PI / num_points;
    	double theta, phi;
    	double [] p;

    	ArrayList<double []> points = new ArrayList<double []>();
    	ArrayList<Integer> edges = new ArrayList<Integer>();
    	ArrayList<double []> normals = new ArrayList<double []>();
    	ArrayList<double []> tex = new ArrayList<double []>();

    	theta = Math.PI;
    	phi = Math.PI / 2;

    	for(int row = 0; row < num_points; row++)
    	{
    		for(int col = 0; col < num_points; col++)
    		{
    			p = new double[3];
    			p[0] = Math.sin(theta) * Math.cos(phi - as);
    			p[1] = Math.cos(theta) * Math.cos(phi - as);
    			p[2] = Math.sin(phi - as);
    			points.add(p);
    			normals.add(p);
    			tex.add(new double [] {0, 0});
    			
    			p = new double[3];
    			p[0] = Math.sin(theta + 2 * as) * Math.cos(phi - as);
    			p[1] = Math.cos(theta + 2 * as) * Math.cos(phi - as);
    			p[2] = Math.sin(phi - as);
    			points.add(p);
    			normals.add(p);
    			tex.add(new double [] {1, 0});

    			p = new double[3];
    			p[0] = Math.sin(theta + 2 * as) * Math.cos(phi);
    			p[1] = Math.cos(theta + 2 * as) * Math.cos(phi);
    			p[2] = Math.sin(phi);
    			points.add(p);
    			normals.add(p);
    			tex.add(new double [] {1, 1});

    			p = new double[3];
    			p[0] = Math.sin(theta) * Math.cos(phi);
    			p[1] = Math.cos(theta) * Math.cos(phi);
    			p[2] = Math.sin(phi);
    			points.add(p);
    			normals.add(p);
    			tex.add(new double [] {0, 1});

    			// make triangles
    			edges.add(points.size()-1);
    			edges.add(points.size()-3);
    			edges.add(points.size()-4);

    			edges.add(points.size()-1);
    			edges.add(points.size()-2);
    			edges.add(points.size()-3);
    			
    			theta -= 2 * as;
    		}
    		
    		phi -= as;
    	}
    	
    	sphereVertices = new double[points.size() * 3];
    	sphereTexcoords = new double[tex.size() * 2];
    	sphereNormals = new double[normals.size() * 3];
    	sphereIndices = new short[edges.size() * 1];

    	int counter = 0;
    	
    	for(int c1 = 0; c1 < points.size(); c1++)
    	{
    		sphereVertices[counter++] = points.get(c1)[0];
    		sphereVertices[counter++] = points.get(c1)[1];
    		sphereVertices[counter++] = points.get(c1)[2];
    	}

    	counter = 0;
    	
    	for(int c1 = 0; c1 < tex.size(); c1++)
    	{
    		sphereTexcoords[counter++] = tex.get(c1)[0];
    		sphereTexcoords[counter++] = tex.get(c1)[1];
    	}

    	counter = 0;

    	for(int c1 = 0; c1 < normals.size(); c1++)
    	{
    		sphereNormals[counter++] = normals.get(c1)[0];
    		sphereNormals[counter++] = normals.get(c1)[1];
    		sphereNormals[counter++] = normals.get(c1)[2];
    	}

    	for(int c1 = 0; c1 < edges.size(); c1++)
    	{
    		sphereIndices[c1] = edges.get(c1).shortValue();
    	}
    	
        mVertBuff = fillBuffer(sphereVertices);
        mTexCoordBuff = fillBuffer(sphereTexcoords);
        mNormBuff = fillBuffer(sphereNormals);
        mIndBuff = fillBuffer(sphereIndices);
    }

    public SphereObject(float tx, float ty, float tz, float rx, float ry, float rz, float sx, float sy, float sz)
    {
    	super(tx, ty, tz, rx, ry, rz, sx, sy, sz);
    	
        mVertBuff = fillBuffer(sphereVertices);
        mTexCoordBuff = fillBuffer(sphereTexcoords);
        mNormBuff = fillBuffer(sphereNormals);
        mIndBuff = fillBuffer(sphereIndices);
    }

    // copy constructor
    public SphereObject(SphereObject c)
    {
    	super(c);
    	
        mVertBuff = fillBuffer(sphereVertices);
        mTexCoordBuff = fillBuffer(sphereTexcoords);
        mNormBuff = fillBuffer(sphereNormals);
        mIndBuff = fillBuffer(sphereIndices);
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
        return sphereVertices.length / 3;
    }
    
    
    @Override
    public int getNumObjectIndex()
    {
        return sphereIndices.length;
    }
    
    public SphereObject copy()
    {
    	return new SphereObject(this);
    }
}
