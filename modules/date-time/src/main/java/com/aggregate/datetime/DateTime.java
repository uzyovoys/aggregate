package com.aggregate.datetime;

import com.aggregate.api.Markup;
import com.aggregate.api.Pattern;
import com.aggregate.api.Request;
import io.vertx.core.AbstractVerticle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by morfeusys on 23.02.16.
 */
public class DateTime extends AbstractVerticle {
    private final DateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private final DateFormat dateFormat = new SimpleDateFormat("dd MMMM, EEEE", new Locale("ru")); //todo best approach

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer("cmd.datetime.time", m -> {
            m.reply(getTime());
        });
        vertx.eventBus().consumer("cmd.datetime.date", m -> {
            m.reply(getDate(Request.fromMessage(m)));
        });
    }

    private String getTime() {
        return timeFormat.format(new Date());
    }

    private String getDate(Request request) {
        Markup date = request.markup.get(Pattern.DATE);
        Date d;
        if (date != null) {
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.set(Calendar.DATE, (Integer) date.data.get("day"));
            calendar.set(Calendar.MONTH, (Integer) date.data.get("month"));
            calendar.set(Calendar.YEAR, (Integer) date.data.get("year"));
            d = calendar.getTime();
        } else {
            d = new Date();
        }
        return dateFormat.format(d);
    }
}
