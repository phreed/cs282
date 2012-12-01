package edu.vanderbilt.cs282.feisele.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


public class AmmoMockProvider01 extends AmmoMockProviderBase {

  protected class AmmoMockDatabaseHelper extends AmmoMockProviderBase.AmmoMockDatabaseHelper {

    protected AmmoMockDatabaseHelper(Context context) {
      super(context, (String) null, (SQLiteDatabase.CursorFactory) null, AmmoMockSchema01.DATABASE_VERSION);
    }

  }

  @Override
  public boolean createDatabaseHelper() {
    this.openHelper = new AmmoMockProviderBase.AmmoMockDatabaseHelper(getContext(), null, null, AmmoMockSchema01.DATABASE_VERSION) {

    };
    return true;
  }
 
  
  protected AmmoMockProvider01( Context context ) {
    super(context);
  }
  
  public static AmmoMockProvider01 getInstance(Context context) {
     AmmoMockProvider01 f = new AmmoMockProvider01(context);
     f.onCreate();
     return f;
  }
  
  public SQLiteDatabase getDatabase() {
    return this.openHelper.getWritableDatabase();
  }
  
  public void release() {
      this.openHelper.close();
  }
}
