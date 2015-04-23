package com.example.jnewel200.sunshine.app;

import android.test.AndroidTestCase;

/**
 * Created by jnewel200 on 4/23/2015.
 */
public class UtilityTest extends AndroidTestCase {

    public void testSettingAndGettingLastRefreshed(){
        long initialVal = Utility.getLastRefreshed(mContext);

        try {
            long testval = 111222444;
            Utility.setLastRefreshed(testval, mContext);
            long valueback = Utility.getLastRefreshed(mContext);
            assertEquals(testval, valueback);
        }finally {
            try{
                Utility.setLastRefreshed(initialVal,mContext);
            }catch (Exception e)
            {   }
        }

    }

}
