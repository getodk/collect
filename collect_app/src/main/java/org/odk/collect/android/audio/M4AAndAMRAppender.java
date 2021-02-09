package org.odk.collect.android.audio;

import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.AppendTrack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class M4AAndAMRAppender implements AudioFileAppender {

    @Override
    public void append(File one, File two) throws IOException {
        if (one.getName().endsWith(".m4a")) {
            combineMP4Files(one, two);
        } else if (one.getName().endsWith(".amr")) {
            combineAMRFiles(one, two);
        } else {
            throw new IllegalArgumentException("Using unknown container for recording!");
        }
    }

    private void combineMP4Files(File existingFile, File newFile) throws IOException {
        Track existingTrack = MovieCreator.build(existingFile.getAbsolutePath()).getTracks().get(0);
        Track newTrack = MovieCreator.build(newFile.getAbsolutePath()).getTracks().get(0);

        Movie movie = new Movie();
        movie.addTrack(new AppendTrack(existingTrack, newTrack));

        Container container = new DefaultMp4Builder().build(movie);

        try (FileChannel fileChannel = new RandomAccessFile(existingFile, "rw").getChannel()) {
            container.writeContainer(fileChannel);
        }
    }

    private void combineAMRFiles(File outputFile, File inputFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputFile, true);
        FileInputStream fis = new FileInputStream(inputFile);

        byte[] fileContent = new byte[(int) inputFile.length()];
        fis.read(fileContent);

        byte[] headerlessFileContent = new byte[fileContent.length - 6];
        if (fileContent.length - 6 >= 0) {
            System.arraycopy(fileContent, 6, headerlessFileContent, 0, fileContent.length - 6);
        }

        fileContent = headerlessFileContent;
        fos.write(fileContent);
    }
}
