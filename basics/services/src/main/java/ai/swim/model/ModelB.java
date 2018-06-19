package ai.swim.model;

import recon.Form;
import recon.ReconName;
import recon.Value;

import java.util.Objects;

/**
 * Use this pattern to convert a POJO to a Recon Value
 * The value in the @ReconName annotation associated with the class name will be used as the header of the Recon
 * representation for all objects of type ModelB
 *
 * The value in the @ReconName annotation associated with a field will be used in the Recon representation of that field
 *
 * Eg: Consider a ModelB object has the following fields: {bool=false, str="1", intg=1, lng=1L, flt=1.0, dbl=1.0
 *     It's recon representation will be: @modelB{b:false,s:"1",i:1,l:1,f:1,d:1}
 *
 * Refer to the toValue method to get the Recon Value for a POJO instance
 */

@ReconName("modelB")
public class ModelB {

  @ReconName("b")
  private Boolean bool;

  @ReconName("s")
  private String str;

  @ReconName("i")
  private Integer intg;

  @ReconName("l")
  private Long lng;

  @ReconName("f")
  private Float flt;

  @ReconName("d")
  private Double dbl;

  public ModelB() {
  }

  public static Form<ModelB> FORM = Form.forClass(ModelB.class);

  public ModelB(Boolean bool, String str, Integer intg, Long lng, Float flt, Double dbl) {
    this.bool = bool;
    this.str = str;
    this.intg = intg;
    this.lng = lng;
    this.flt = flt;
    this.dbl = dbl;
  }

  public Boolean getBool() {
    return bool;
  }

  public String getStr() {
    return str;
  }

  public Integer getIntg() {
    return intg;
  }

  public Long getLng() {
    return lng;
  }

  public Float getFlt() {
    return flt;
  }

  public Double getDbl() {
    return dbl;
  }

  public Value toValue() {
    return FORM.mold(this);
  }

  @Override
  public String toString() {
    return "ModelB{" +
        "bool=" + bool +
        ", str='" + str + '\'' +
        ", intg=" + intg +
        ", lng=" + lng +
        ", flt=" + flt +
        ", dbl=" + dbl +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ModelB modelB = (ModelB) o;
    return Objects.equals(bool, modelB.bool) &&
        Objects.equals(str, modelB.str) &&
        Objects.equals(intg, modelB.intg) &&
        Objects.equals(lng, modelB.lng) &&
        Objects.equals(flt, modelB.flt) &&
        Objects.equals(dbl, modelB.dbl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(bool, str, intg, lng, flt, dbl);
  }
}
