package de.unikn.widening.test.framework;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Function;

import de.unikn.widening.base.WideningModel;

public class FileTestResultListener<S extends Comparable<S>, T extends WideningModel<S>> implements TestResultListener<S,T> {

    private FileOutputStream m_fos;
    private final Function<T,String> m_toString;

    public FileTestResultListener(final String fileName, final Function<T,String> toString) throws FileNotFoundException {
        m_fos = new FileOutputStream(fileName);
        m_toString = toString;
    }

    @Override
    public void resultAvailable(final TestResult<S,T> result) {
        try {
            result.writeScores(m_fos, m_toString);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            m_fos.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
