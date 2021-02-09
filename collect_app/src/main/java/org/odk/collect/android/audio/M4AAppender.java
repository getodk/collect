package org.odk.collect.android.audio;

import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.AppendTrack;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class M4AAppender implements AudioFileAppender {

    @Override
    public void append(File one, File two) throws IOException {
        Track existingTrack = MovieCreator.build(one.getAbsolutePath()).getTracks().get(0);
        Track newTrack = MovieCreator.build(two.getAbsolutePath()).getTracks().get(0);

        Movie movie = new Movie();
        movie.addTrack(new AppendTrack(existingTrack, newTrack));

        Container container = new DefaultMp4Builder().build(movie);

        try (FileChannel fileChannel = new RandomAccessFile(one, "rw").getChannel()) {
            container.writeContainer(fileChannel);
        }
    }
}
