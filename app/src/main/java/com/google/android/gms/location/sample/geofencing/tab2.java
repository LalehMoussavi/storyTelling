package com.google.android.gms.location.sample.geofencing;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class tab2 extends Fragment implements View.OnClickListener{

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,
                             Bundle savedInstanceState){
        View rootView=inflater.inflate(R.layout.tab2, container, false);
        FloatingActionButton play = (FloatingActionButton) rootView.findViewById(R.id.play);
        FloatingActionButton stop = (FloatingActionButton) rootView.findViewById(R.id.stop);
        play.setOnClickListener(this);
        stop.setOnClickListener(this);
        return rootView;
        }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("switching tabs");
        MyTTS.stop();
    }

    @Override
    public void onClick(View rootView) {
        switch (rootView.getId()) {
            case R.id.play:
                MyTTS.play("The Medieval old town was very different to the one you see now. Wooden houses line to top of the Royal Mile ridge and pastures stretch down slopes on either side. In fact early Edinburgh seems to been quite disagreeable place to live. Well, as disagreeable as the town riven with internal strife, possessing no sanitation, no medicine, no law enforcement and constantly fighting either the Highlanders or the English. Bad fighting; however, finally led to the erection of a 16th century barrier called the Flodden wall right around the town, which was a very bad idea! Further the wall was spectacularly useless at keeping invaders out, it effectively stopped Edinburgh from expanding. Edinburgh became an utterly unfavorable place to live and not only because of the overcrowding. Religious persecution, plague, Civil War, invasion ,famine, poverty, crime, it had it all. Then in the 18th century the wall finally came down and the capital went from the squalor little backwater to fame as the Athens of the North. Except all the smart money then moved to the new town and into the Old Town purred the different population. Highlanders thrown off their lands to make way for sheep, lowlanders lured to the City by the agricultural and industrial revolutions. Also, flood of immigrants from Ireland where the potato famine had cost mass starvation. All were dirt poor living in incredible squalor. Things stay that way until the slum clearances on the 20th century. With history like this no wonder there are so many ghosts around, but I'll let you find that out for yourself.",false);
                break;
        case R.id.stop:
            MyTTS.stop();
            break;
        }

    }

}
