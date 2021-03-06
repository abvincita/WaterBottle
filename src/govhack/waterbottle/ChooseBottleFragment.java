package govhack.waterbottle;
/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * A fragment representing a single step in a wizard. The fragment shows a dummy title indicating
 * the page number, along with some dummy text.
 *
 * <p>This class is used by the {@link CardFlipActivity} and {@link
 * ScreenSlideActivity} samples.</p>
 */
public class ChooseBottleFragment extends Fragment implements OnClickListener {
    /**
     * The argument key for the page number this fragment represents.
     */
    public static final String ARG_PAGE = "page";

    /**
     * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
     */
    private int mPageNumber;
    private static ViewPager pagerView;

    /**
     * Factory method for this fragment class. Constructs a new fragment for the given page number.
     */
    public static ChooseBottleFragment create(int pageNumber, ViewPager parent) {
    	ChooseBottleFragment fragment = new ChooseBottleFragment();
    	pagerView = parent;
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ChooseBottleFragment() 
    { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.choose_bottle_fragment, container, false);
        
        ImageButton button = (ImageButton) rootView.findViewById(R.id.finish_tut_button);
        button.setVisibility(View.VISIBLE);	
        button.setOnClickListener(this);
        
        int density = getResources().getDisplayMetrics().densityDpi;
        if(density <= DisplayMetrics.DENSITY_HIGH)
        {
        	button.setMinimumHeight(60);
        }
        
        ImageView previousArrow = (ImageView) rootView.findViewById(R.id.previous_arrow);
        previousArrow.setVisibility(View.INVISIBLE);
        previousArrow.setOnClickListener(this);
        
        ImageView nextArrow = (ImageView) rootView.findViewById(R.id.next_arrow);
        nextArrow.setOnClickListener(this);

        ImageView tutorialImage = (ImageView) rootView.findViewById(R.id.tutorial_image);
        
        switch(mPageNumber+1)
        {
        	case 1:
        		break;
        	case 2:
        		tutorialImage.setImageResource(R.drawable.circle2);
        		break;
        	case 3:
        		tutorialImage.setImageResource(R.drawable.circle3);
        		break;
        	case 4:
        		tutorialImage.setImageResource(R.drawable.circle4);
        		break;
        	default:
        		break;
        }
        
        if(mPageNumber + 1 > 1)
        {
        	previousArrow.setVisibility(View.VISIBLE);
        }
        
        if(mPageNumber + 1 == HomeActivity.NUM_PAGES)
        {
        	nextArrow.setVisibility(View.INVISIBLE);      	
        }

        return rootView;
    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return mPageNumber;
    }
    
    @Override
    public void onClick(View v) 
    {
        switch (v.getId()) 
        {
        	case R.id.finish_tut_button:
        		
        		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        	    SharedPreferences.Editor editor = settings.edit();
        	    editor.putBoolean(HomeActivity.CHOOSE_BOTTLE_SETTING, false);

        	    HomeActivity.chooseBottleTitle.setVisibility(View.GONE);
        	    
        	    switch(mPageNumber+1)
                {
                	case 1:
                		editor.putInt(HomeActivity.BOTTLE_CHOICE, 1);
                		break;
                	case 2:
                		editor.putInt(HomeActivity.BOTTLE_CHOICE, 2);
                		HomeActivity.waterBottle.setImageResource(R.drawable.holo2);
                		break;
                	case 3:
                		editor.putInt(HomeActivity.BOTTLE_CHOICE, 3);
                		HomeActivity.waterBottle.setImageResource(R.drawable.holo3);
                		break;
                	case 4:
                		editor.putInt(HomeActivity.BOTTLE_CHOICE, 4);
                		HomeActivity.waterBottle.setImageResource(R.drawable.holo4);
                		break;
                	default:
                		break;
                }
        	    
        	    // Commit the edits!
        	    editor.commit();

        		ViewGroup vg = (ViewGroup)(pagerView.getParent());
        		vg.removeView(pagerView);
        		
        		HomeActivity.increaseWater(1000);
        		break;
        		
        	case R.id.next_arrow:
        		pagerView.setCurrentItem(mPageNumber + 1, true);
        		break;
        		
        	case R.id.previous_arrow:
        		pagerView.setCurrentItem(mPageNumber - 1, true);
        		break;
        }
    }
}