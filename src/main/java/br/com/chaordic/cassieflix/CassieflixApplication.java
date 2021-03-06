package br.com.chaordic.cassieflix;

import com.fasterxml.jackson.databind.DeserializationFeature;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import br.com.chaordic.cassieflix.db.client.CassandraClient;
import br.com.chaordic.cassieflix.db.client.SyncCassandraClient;
import br.com.chaordic.cassieflix.db.dao.MovieDao;
import br.com.chaordic.cassieflix.db.dao.MovieListDao;
import br.com.chaordic.cassieflix.db.dao.SimilarMoviesDao;
import br.com.chaordic.cassieflix.db.dao.UserActivityDao;
import br.com.chaordic.cassieflix.db.dao.UserDao;
import br.com.chaordic.cassieflix.db.dao.cassandra.MovieCassandraDao;
import br.com.chaordic.cassieflix.db.dao.cassandra.MovieListCassandraDao;
import br.com.chaordic.cassieflix.db.dao.cassandra.SimilarMoviesCassandraDao;
import br.com.chaordic.cassieflix.db.dao.cassandra.UserActivityCassandraDao;
import br.com.chaordic.cassieflix.db.dao.cassandra.UserCassandraDao;
import br.com.chaordic.cassieflix.health.CassandraHealthCheck;
import br.com.chaordic.cassieflix.resources.MovieListResource;
import br.com.chaordic.cassieflix.resources.MoviesResource;
import br.com.chaordic.cassieflix.resources.SimilarMoviesResource;
import br.com.chaordic.cassieflix.resources.StatusResource;
import br.com.chaordic.cassieflix.resources.UserActivityResource;
import br.com.chaordic.cassieflix.resources.UsersResource;

public class CassieflixApplication extends Application<CassieflixConfiguration> {
    public static void main(String[] args) throws Exception {
        new CassieflixApplication().run(args);
    }

    @Override
    public String getName() {
        return "cassieflix";
    }

    @Override
    public void initialize(Bootstrap<CassieflixConfiguration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(CassieflixConfiguration conf, Environment env) {
        CassandraClient cassieClient = new SyncCassandraClient(conf.getCassandraInitializer());

        /* DAOs */
        MovieDao movieDao = new MovieCassandraDao(cassieClient);
        SimilarMoviesDao similarMoviesDao = new SimilarMoviesCassandraDao(cassieClient);
        MovieListDao movieListDao = new MovieListCassandraDao(cassieClient);        
        UserDao userDao = new UserCassandraDao(cassieClient);
        UserActivityDao userActivityDao = new UserActivityCassandraDao(cassieClient);        

        /* Health Checks */
        env.healthChecks().register("cassandra", new CassandraHealthCheck(cassieClient));
        //env.getObjectMapper().configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        
        /* Resources */
        env.jersey().register(new MoviesResource(movieDao));
        env.jersey().register(new SimilarMoviesResource(movieDao, similarMoviesDao));        
        env.jersey().register(new MovieListResource(movieDao, movieListDao));        
        env.jersey().register(new UsersResource(userDao, movieListDao));	
        env.jersey().register(new UserActivityResource(userActivityDao));                
        env.jersey().register(new StatusResource(cassieClient));
    }

}
