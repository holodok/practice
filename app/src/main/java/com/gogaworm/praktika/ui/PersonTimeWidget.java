package com.gogaworm.praktika.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.gogaworm.praktika.R;
import com.gogaworm.praktika.data.PersonWorkingTime;
import com.gogaworm.praktika.data.TimeUnit;

/**
 * Created on 08.03.2017.
 *
 * @author ikarpova
 */
public class PersonTimeWidget extends FrameLayout {
    private TextView personNameView;
    private TextView personLabelView;
    private TextView workingTimeView;
    private View colorBoxView;

    private StudentColorSelector studentColorSelector;

    public PersonTimeWidget(Context context) {
        super(context);
        init(context);
    }

    public PersonTimeWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PersonTimeWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        studentColorSelector = new StudentColorSelector(context);

        View.inflate(context, R.layout.widget_person_time, this);

        colorBoxView = findViewById(R.id.colorBox);
        personLabelView = (TextView) findViewById(R.id.personLabel);
        personNameView = (TextView) findViewById(R.id.personName);
        workingTimeView = (TextView) findViewById(R.id.workingTime);
    }

    public void setup(PersonWorkingTime personWorkingTime, int colorPosition) {
         colorBoxView.setBackgroundColor(studentColorSelector.getColor(colorPosition));
        personLabelView.setVisibility(VISIBLE);
        personLabelView.setText(personWorkingTime.person.type == 0 ? R.string.label_student : R.string.label_teacher);
        personNameView.setText(personWorkingTime.person.name);
        workingTimeView.setText(personWorkingTime.workingTime.toString());
    }

    public void setupGeneral(TimeUnit timeUnit, boolean exceeds) {
        colorBoxView.setBackgroundColor(getResources().getColor(R.color.chartFinalColor));
        personLabelView.setVisibility(GONE);
        personNameView.setText(R.string.label_general_time);
        workingTimeView.setText(getResources().getString(R.string.label_time_format, timeUnit.hour, timeUnit.minute));
        workingTimeView.setTextColor(exceeds ? Color.RED : getResources().getColor(R.color.primaryText));
    }
}
