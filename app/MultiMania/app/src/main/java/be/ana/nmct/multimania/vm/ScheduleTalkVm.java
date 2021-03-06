package be.ana.nmct.multimania.vm;

import java.util.Date;

import be.ana.nmct.multimania.model.Talk;

/**
 * The viewModel for a Talk
 * @see be.ana.nmct.multimania.model.Talk
 * Created by Niels on 19/11/2014.
 */
public class ScheduleTalkVm extends Talk {
    public String tags;
    public String room;
    public String fromString;
    public String untilString;
    public boolean isDoubleBooked;

    public ScheduleTalkVm() {
    }

    public ScheduleTalkVm(int id, String title, Date from, Date to, String description, int roomId, boolean isKeynote) {
        this.id = id;
        this. title = title;
        this.from = from;
        this.to = to;
        this.description = description;
        this.roomId = roomId;
        this.isKeynote = isKeynote;
    }

    public ScheduleTalkVm(String fromString, String untilString) {
        this.fromString = fromString;
        this.untilString = untilString;
    }
}
