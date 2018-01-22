package example.configurationservice.local.dao;

import example.configuration.type.Configuration;
import example.configuration.type.ConfigurationList;
import example.configurationservice.local.exception.ConfigurationRepositoryException;


/**
 * DAO interface of readonly configuration operation
 */
public interface ReadonlyConfigurationDAO {

    /**
     * Select a configuration by the name, if configuration does not
     * exist just return null
     *
     * @param name
     * @return {@link Configuration}
     * @throws ConfigurationRepositoryException
     */
    Configuration selectConfigurationByName(String name) throws ConfigurationRepositoryException;

    /**
     * Select a configuration list by the fuzzy name. If no
     * configurations fuzzy match the name, this method will still
     * return a {@link ConfigurationList} with a null list. <br/>
     * <p/>
     * The fuzzy name:
     * <ul>
     * <li>case sensitive</li>
     * <li>no configurations will match a fuzyy name with null value
     * </li>
     * </ul>
     *
     * @param fuzzyName fuzzy name to search
     * @return {@link ConfigurationList}
     * @throws ConfigurationRepositoryException
     */
    ConfigurationList selectConfigurationByFuzzyName(String fuzzyName) throws ConfigurationRepositoryException;

}