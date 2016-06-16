package com.company;

import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class BeatBoxer{
    JFrame frame;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JPanel mainPanel;
    ArrayList<JCheckBox> checkBoxesList;


    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom",
            "Hi Bonco", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};
    int[] instrumentsNumbers = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args) {
        new BeatBoxer().buildGUI();
    }

    public void buildGUI() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BorderLayout borderLayout = new BorderLayout();
        JPanel panel = new JPanel(borderLayout);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkBoxesList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton tempoUp = new JButton("Temp Up");
        tempoUp.addActionListener(new MyTempoUpListener());
        buttonBox.add(tempoUp);

        JButton tempoDown = new JButton("Temp Down");
        tempoDown.addActionListener(new MyTempoDownListener());
        buttonBox.add(tempoDown);

        JButton saveButton = new JButton("Save sequence");
        saveButton.addActionListener(new BeatBoxSaveListener());
        buttonBox.add(saveButton);

        JButton restoreButton = new JButton("Restore sequence");
        restoreButton.addActionListener(new BeatBoxRestoreListener());
        buttonBox.add(restoreButton);

        Box nameBox = new Box(BoxLayout.Y_AXIS);

        for (int i = 0; i < 16; i++)
        {
            nameBox.add(new Label(instrumentNames[i]));
        }


        panel.add(BorderLayout.WEST, nameBox);
        panel.add(BorderLayout.EAST, buttonBox);
        frame.getContentPane().add(panel);

        GridLayout gridLayout = new GridLayout(16, 16);
        gridLayout.setVgap(1);
        gridLayout.setHgap(2);
        mainPanel = new JPanel(gridLayout);

        panel.add(BorderLayout.CENTER, mainPanel);

        for(int i = 0; i < 256; i++)
        {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkBoxesList.add(c);
            mainPanel.add(c);
        }

        setUpMidi();

        frame.setBounds(50, 50, 300, 300);
        frame.pack();
        frame.setVisible(true);
    }

    public void setUpMidi()
    {
        try
        {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);

            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);

        }
        catch (Exception ex){}

    }

    public void buildTrackAndStart()
    {
        int[] trackList = null;
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for(int i = 0; i < 16; i++)
        {
            trackList = new int[16];
            int key = instrumentsNumbers[i];

            for(int j = 0; j < 16; j++)
            {
                JCheckBox jCheckBox = (JCheckBox)checkBoxesList.get(j + (16*i));
                if(jCheckBox.isSelected())
                {
                    trackList[j] = key;
                }
                else
                {
                    trackList[j] = 0;
                }


            }
            makeTracks(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));

        }

        track.add(makeEvent(192, 9, 1, 0, 15));

        try
        {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);

        }
        catch (Exception ex){ex.printStackTrace();}


    }

    public void makeTracks(int[] trackList)
    {
        for (int i = 0; i < 16; i++)
        {
            int key = trackList[i];
            if(key != 0)
            {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i + 1));
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick)
    {
        MidiEvent midiEvent = null;
        try
        {
            ShortMessage shortMessage = new ShortMessage();
            shortMessage.setMessage(comd, chan, one, two);
            midiEvent = new MidiEvent(shortMessage, tick);
        }
        catch (Exception ex){ex.printStackTrace();}

        return midiEvent;
    }

    class MyStartListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            buildTrackAndStart();
        }
    }

    class MyStopListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            sequencer.stop();
        }
    }

    class MyTempoUpListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 1.03));
        }
    }

    class MyTempoDownListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 0.97));
        }
    }

    class BeatBoxSaveListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            checkBoxStateSaving();
        }
    }

    class BeatBoxRestoreListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            checkBoxStateRestoring();
        }
    }


    public void checkBoxStateSaving()
    {
        boolean[] checkBoxesState = new boolean[256];
        for(int i = 0; i < 256; i++)
        {
            JCheckBox box = checkBoxesList.get(i);
            if(box.isSelected())
            {
                checkBoxesState[i] = true;
            }
            else
            {
                checkBoxesState[i] = false;
            }

        }

        try
        {
            JFileChooser chooser = new JFileChooser();
            chooser.showSaveDialog(frame);
            File file = chooser.getSelectedFile();

            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
            os.writeObject(checkBoxesState);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println("Serialisation has been not complete successful!");
        }
    }

    public void checkBoxStateRestoring()
    {
        boolean[] checkBoxesState;
        try
        {
            JFileChooser chooser = new JFileChooser();
            chooser.showOpenDialog(frame);
            File file = chooser.getSelectedFile();

            ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
            checkBoxesState = (boolean[]) is.readObject();

            for (int i = 0; i < 256; i++)
            {
                JCheckBox box = (JCheckBox) checkBoxesList.get(i);
                box.setSelected(checkBoxesState[i]);
            }


        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println("We couldn't read this file");
        }

        sequencer.stop();
        buildTrackAndStart();
    }
}
