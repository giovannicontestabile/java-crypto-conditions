package com.bigchaindb.cryptoconditions.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.EnumSet;

import com.bigchaindb.cryptoconditions.CompoundCondition;
import com.bigchaindb.cryptoconditions.CompoundSha256Condition;
import com.bigchaindb.cryptoconditions.Condition;
import com.bigchaindb.cryptoconditions.ConditionType;
import com.bigchaindb.cryptoconditions.der.DEROutputStream;
import com.bigchaindb.cryptoconditions.der.DERTags;

public class PrefixSha256Condition extends CompoundSha256Condition implements CompoundCondition
{
  private byte[] prefix;
  private long maxMessageLength;
  private Condition subcondition;
  
  public PrefixSha256Condition(byte[] prefix, long maxMessageLength, Condition subcondition)
  {
    super(calculateCost(prefix, maxMessageLength, subcondition.getCost()), calculateSubtypes(subcondition));
    this.prefix = new byte[prefix.length];
    System.arraycopy(prefix, 0, this.prefix, 0, prefix.length);
    this.maxMessageLength = maxMessageLength;
    this.subcondition = subcondition;
  }
  
  public PrefixSha256Condition(byte[] fingerprint, long cost, EnumSet<ConditionType> subtypes) {
    super(fingerprint, cost, subtypes);
  }
  
  public ConditionType getType()
  {
    return ConditionType.PREFIX_SHA256;
  }
  

  protected byte[] getFingerprintContents()
  {
    try
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DEROutputStream out = new DEROutputStream(baos);
      out.writeTaggedObject(0, prefix);
      out.writeTaggedObject(1, BigInteger.valueOf(maxMessageLength).toByteArray());
      out.writeTaggedConstructedObject(2, subcondition.getEncoded());
      out.close();
      byte[] buffer = baos.toByteArray();
      

      baos = new ByteArrayOutputStream();
      out = new DEROutputStream(baos);
      out.writeEncoded(DERTags.CONSTRUCTED.getTag() + DERTags.SEQUENCE.getTag(), buffer);
      out.close();
      return baos.toByteArray();
    }
    catch (IOException e) {
      throw new RuntimeException("DER Encoding Error", e);
    }
  }
  









  private static long calculateCost(byte[] prefix, long maxMessageLength, long subconditionCost)
  {
    return prefix.length + maxMessageLength + subconditionCost + 1024L;
  }
  
  private static EnumSet<ConditionType> calculateSubtypes(Condition subcondition) {
    EnumSet<ConditionType> subtypes = EnumSet.of(subcondition.getType());
    if ((subcondition instanceof CompoundCondition)) {
      subtypes.addAll(((CompoundCondition)subcondition).getSubtypes());
    }
    

    if (subtypes.contains(ConditionType.PREFIX_SHA256)) {
      subtypes.remove(ConditionType.PREFIX_SHA256);
    }
    
    return subtypes;
  }
}
