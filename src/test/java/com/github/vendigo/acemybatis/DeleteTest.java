package com.github.vendigo.acemybatis;

import com.github.vendigo.acemybatis.test.app.AbstractTest;
import com.github.vendigo.acemybatis.test.app.User;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class DeleteTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        reInsertAllGuys();
    }

    @Test
    public void deleteAll() throws Exception {
        int rowCount = userMapper.deleteAll();
        assertThat(rowCount, equalTo(5));
        List<User> actualResults = userTestDao.selectAll();
        assertThat(actualResults, hasSize(0));
    }

    @Test
    public void deleteByEmail() throws Exception {
        int rowCount = userMapper.deleteByEmail("boris50@gmail.com");
        assertThat(rowCount, equalTo(1));
        List<User> actualResults = userTestDao.selectAll();
        assertCollections(actualResults, asList(petya, eric, galya, ostin));
    }

    @Test
    public void syncDelete() throws Exception {
        int rowCount = userMapper.syncDelete(asList(petya, galya));
        assertThat(rowCount, equalTo(2));
        List<User> actualResults = userTestDao.selectAll();
        assertCollections(actualResults, asList(eric, boris, ostin));
    }

    @Test
    public void asyncDelete() throws Exception {
        int rowCount = userMapper.asyncDelete(asList(petya, galya)).get();
        assertThat(rowCount, equalTo(2));
        List<User> actualResults = userTestDao.selectAll();
        assertCollections(actualResults, asList(eric, boris, ostin));
    }

    @Test
    public void deleteCollector() throws Exception {
        int rowCount = userMapper.asyncDelete(asList(petya, galya)).get();
        List<User> collected = Stream.of(petya, galya).collect(userMapper.deleteCollector());
        assertThat(rowCount, equalTo(2));
        List<User> actualResults = userTestDao.selectAll();
        assertCollections(actualResults, asList(eric, boris, ostin));
        assertCollections(collected, asList(petya, galya));
    }
}
