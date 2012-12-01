package edu.vanderbilt.cs282.feisele.provider;

public class AmmoMockSchema01 extends AmmoMockSchemaBase {


  public static final int DATABASE_VERSION = 4;

  public static final String DATABASE_NAME = ":memory";


  public static class AmmoTableSchema extends AmmoTableSchemaBase {

    protected AmmoTableSchema() { } // no instantiation
  }

  public static class QuickTableSchema extends QuickTableSchemaBase {

    protected QuickTableSchema() { } // no instantiation
  }

  public static class StartTableSchema extends StartTableSchemaBase {

    protected StartTableSchema() { } // no instantiation
  }
}
