package com.sotrender.cardinality;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.io.LittleEndianDataInputStream;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import org.apache.commons.codec.binary.Base64;

public class HyperLogLogPlusTest {

  @Test
  public void HyperLogLogPlus() {
    HyperLogLogPlus obj = new HyperLogLogPlus(16);

    Assert.assertNotNull(obj);
  }

  @Test
  public void cardinality() {
    HyperLogLogPlus obj = new HyperLogLogPlus(16);
    obj.offer("value1");
    obj.offer("value2");

    Assert.assertEquals( obj.cardinality(), 2L);
  }

  @Test
  public void getCcardBytes() throws IOException {
    HyperLogLogPlus obj = new HyperLogLogPlus(16);
    obj.offer("value1");
    obj.offer("value2");

    byte[] output = obj.getCcardBytes();

    Assert.assertEquals( output.length , (1 << 16)+3);
    Assert.assertEquals( output[0], (byte) 4 );
    Assert.assertEquals( output[1], (byte) 3 );
    Assert.assertEquals( output[2], (byte) 16 );
  }

  @Test
  public void getCompressedCcardBytes() throws IOException {
    HyperLogLogPlus obj = new HyperLogLogPlus(16);
    obj.offer("value1");
    obj.offer("value2");

    byte[] output = obj.getCompressedCcardBytes();

    Assert.assertEquals( output.length < ((1 << 16)+3), true );

    ByteArrayInputStream bais = new ByteArrayInputStream(output);
    LittleEndianDataInputStream oi = new LittleEndianDataInputStream(bais);

    int size = oi.readInt();
    byte[] input  = new byte[output.length-4];
    byte[] finaloutput = new byte[size];

    oi.readFully(input);

    LZ4Factory factory = LZ4Factory.fastestInstance();
    LZ4FastDecompressor decompress = factory.fastDecompressor();
    decompress.decompress(input, finaloutput);

    Assert.assertEquals( finaloutput.length , (1 << 16)+3);
    Assert.assertEquals( finaloutput[0], (byte) 4 );
    Assert.assertEquals( finaloutput[1], (byte) 3 );
    Assert.assertEquals( finaloutput[2], (byte) 16 );
  }

  @Test
  public void buildFromCompressedCcardBytes() throws IOException {
    HyperLogLogPlus obj = new HyperLogLogPlus(16);
    obj.offer("value1");
    obj.offer("value2");

    byte[] output = obj.getCompressedCcardBytes();

    HyperLogLogPlus dobj = HyperLogLogPlus.Builder.buildFormCompressedCcard(output);

    Assert.assertNotNull(dobj);
    Assert.assertEquals(dobj.cardinality(), 2L );
  }

  @Test
  public void buildFromCompressedPredefined() throws IOException {
    /* this is compress HLL++ object made using python ccard plugin */
    byte[] output = Base64.decodeBase64("AwEAALEEAwgAAAADAAIDAAEAQwEAAAMLAAEXACAEAgkAAAIAEAYFAAEhACQABQkAEAMuACIDASQAMQMAAAkAUAUDAAAEIgCSAAcAAQAAAAEDGAAgAAEOAAACABABGAABBQBIAAAACBgAARYABSgAMgAAAgwAAAcAEAGjABACOgADAgAQBk8ANQEBAK8AAAIAgQMAAwABAgABowAwAgMGMAAAKAAhAAMGAAACAAEJALACAAAAAAABAAAAAQ==");

    HyperLogLogPlus dobj = HyperLogLogPlus.Builder.buildFormCompressedCcard(output);

    Assert.assertNotNull(dobj);
    Assert.assertEquals(dobj.cardinality(), 92L );
  }
}
