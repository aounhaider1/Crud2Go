package de.unioninvestment.crud2go.testing

import de.unioninvestment.crud2go.testing.db.TestConnectionPoolFactory
import de.unioninvestment.eai.portal.portlet.crud.domain.validation.ModelValidator;
import de.unioninvestment.eai.portal.portlet.crud.config.PortletConfig
import de.unioninvestment.eai.portal.portlet.crud.config.converter.PortletConfigurationUnmarshaller
import de.unioninvestment.eai.portal.portlet.crud.config.resource.Config
import de.unioninvestment.eai.portal.portlet.crud.domain.model.ModelBuilder
import de.unioninvestment.eai.portal.portlet.crud.domain.model.ModelFactory
import de.unioninvestment.eai.portal.portlet.crud.domain.model.ModelPreferences
import de.unioninvestment.eai.portal.portlet.crud.domain.model.Portlet
import de.unioninvestment.eai.portal.portlet.crud.domain.model.PortletRole;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.user.AnonymousUser;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.user.CurrentUser
import de.unioninvestment.eai.portal.portlet.crud.domain.model.user.NamedUser
import de.unioninvestment.eai.portal.portlet.crud.domain.model.user.UserFactory
import de.unioninvestment.eai.portal.portlet.crud.domain.portal.Portal
import de.unioninvestment.eai.portal.portlet.crud.domain.support.PreferencesRepository
import de.unioninvestment.eai.portal.portlet.crud.scripting.model.ScriptModelBuilder
import de.unioninvestment.eai.portal.portlet.crud.scripting.model.ScriptModelFactory
import de.unioninvestment.eai.portal.support.scripting.ConfigurationScriptsCompiler
import de.unioninvestment.eai.portal.support.scripting.ScriptBuilder
import de.unioninvestment.eai.portal.support.scripting.ScriptCompiler
import de.unioninvestment.eai.portal.support.vaadin.mvp.EventBus
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource

import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyString
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.reset
import static org.mockito.Mockito.when

/**
 * Created by cmj on 17.07.14.
 */
@CompileStatic
class CrudTestConfigBuilder {
    private static final PortletConfigurationUnmarshaller unmarshaller = new PortletConfigurationUnmarshaller()

    @TupleConstructor
    private class CacheEntry {
        long compileTime
        PortletConfig config
    }

	@Autowired
	private Portal portalMock
	
    @Autowired
    ScriptModelFactory scriptModelFactory

    @Autowired
    private TestConnectionPoolFactory testConnectionPoolFactory

    @Autowired
    protected ConfigurationScriptsCompiler scriptsCompiler

    @Autowired
    ScriptCompiler scriptCompiler

    @Autowired
    private ModelFactory modelFactory

    @Autowired
    private UserFactory userFactoryMock

    @Autowired
    private TestPreferencesRepository preferencesRepository

    File combinedPath
    Class<?> testClass
    Map<URL, CacheEntry> configCache
	
	private String currentUserName = null
    private Set currentUserRoles = []
    private Set portalRoles = []
	Map<Long,String> roleId2Name = [:]

    private boolean ignoreCachedEntry = false
    private boolean validationEnabled = false

    private Resource configResource

    private ScriptBuilder scriptBuilder = new ScriptBuilder()

    private EventBus eventBus;

    private Map<String, Long> resourceIds = new HashMap<String, Long>();

    private ModelPreferences modelPreferences = new ModelPreferences()

    private Map<String,String> portletPreferences = [:]

    private Statistics statistics = new Statistics()

    CrudTestConfigBuilder() {
        eventBus = new EventBus();
    }

    /**
     * Load the configuration from the following places (match wins)
     * <ul>
     *   <li>A file relative to the combined.path + the package folder of the test class</li>
     *   <li>For paths statring with slash: A path relative to the combined.path of the test class</li>
     *   <li>A classpath resource besides the test class</li>
     *   <li>For paths statring with slash: An absolute classpath entry</li>
     * </ul>
     * @param name
     * @return
     */
    CrudTestConfigBuilder from(String name) {
        File configFile = null
        if (combinedPath?.exists()) {
            if (name.startsWith('/') || name.startsWith('\\')) {
                configFile = new File(combinedPath, name.substring(1))
            } else {
                String relativePath = testClass.package.name.replace('.', '/')
                configFile = new File(new File(combinedPath, relativePath), name)
            }
        }
        if (configFile?.exists()) {
            fromFile(configFile)
        } else {
            fromClasspath name
        }
    }

    CrudTestConfigBuilder fromClasspath(String configFilename) {
        configResource = new ClassPathResource(configFilename, testClass)
        return this
    }

    CrudTestConfigBuilder fromFile(File configFile) {
        configResource = new FileSystemResource(configFile)
        return this
    }

    CrudTestConfigBuilder fromFile(String configFilename) {
        configResource = new FileSystemResource(configFilename)
        return this
    }


    CrudTestConfigBuilder currentUserName(String name) {
		this.currentUserName = name
		return this
	}
	
	CrudTestConfigBuilder currentUserRoles(roles) {
		this.currentUserRoles = roles as HashSet
		return this
	}

    CrudTestConfigBuilder preference(String key, String value) {
        this.portletPreferences[key] = value
        return this
    }

    protected Portlet createModel(PortletConfig configuration) {
        ModelBuilder modelBuilder = createModelBuilder(configuration);
        return modelBuilder.build();
    }

    protected ModelBuilder createModelBuilder(PortletConfig configuration) {
        return modelFactory.getBuilder(eventBus, new Config((PortletConfig)configuration,
                (Map<String, Long>)resourceIds, (String)null, (Date)null), modelPreferences);
    }


    synchronized CrudTestConfig build() {
        reset(portalMock, userFactoryMock);

        long startTime = System.currentTimeMillis()

        PortletConfig portletConfig = prepareConfig()

        long postBuildStartTime = System.currentTimeMillis()
		long roleId = 1
		portletConfig.roles?.role?.each { role ->
			roleId2Name[roleId] = role.name
			resourceIds.put("${CrudTestContext.TEST_PORTLET_ID}_${CrudTestContext.LIFERAY_COMMUNITY_ID}_${role.name}".toString() , roleId++)
        }
		mockCurrentUser()

        ModelBuilder modelBuilder = createModelBuilder(portletConfig)
        Portlet portlet = modelBuilder.build()

		if (validationEnabled) {
			new ModelValidator().validateModel(modelBuilder, portlet, portletConfig)
		}
				
        def validPreferenceKeys = portletConfig.preferences?.preference?.collect { it.@key }
        portletPreferences.each { key, value ->
            if (validPreferenceKeys.contains(key)) {
                preferencesRepository.setPreference(portlet, key, value)
            } else {
                throw new IllegalArgumentException("Unknown preference '$key'. Allowed are $validPreferenceKeys")
            }
        }

        Map mapping = modelBuilder.getModelToConfigMapping()
        ScriptModelBuilder scriptModelBuilder = new ScriptModelBuilder(scriptModelFactory, eventBus,
                testConnectionPoolFactory, userFactoryMock, scriptCompiler, scriptBuilder,
                portlet, mapping)

		long endTime = System.currentTimeMillis()
        statistics.postCompileTime = endTime - postBuildStartTime
        statistics.buildTime = endTime - startTime

        return new CrudTestConfig(portletConfig, scriptModelBuilder.build(), scriptBuilder.mainScript, statistics)
    }

	private mockCurrentUser() {
		when(portalMock.getRoles(currentUserName)).thenReturn(portalRoles);
		when(portalMock.hasPermission(eq(currentUserName), eq("MEMBER"), eq(PortletRole.RESOURCE_KEY), anyString())).thenAnswer(
                { InvocationOnMock inv ->
                    long id = Long.parseLong((String)inv.arguments[3])
                    currentUserRoles.contains(roleId2Name[id])
                } as Answer);
		when(userFactoryMock.getCurrentUser(any(Portlet))).thenAnswer({ InvocationOnMock inv ->
			def user = currentUserName ? new NamedUser(currentUserName, ((Portlet)inv.arguments[0]).roles)
							: new AnonymousUser()
			return new CurrentTestUser(user)
		} as Answer<CurrentUser>)
	}

    CrudTestConfigBuilder recompile() {
        ignoreCachedEntry = true
        return this
    }

    CrudTestConfigBuilder validate() {
        validationEnabled = true
        return this
    }

    private PortletConfig prepareConfig() {
        assert configResource: 'No configuration source given'

        CacheEntry existingEntry = configCache[configResource.URL]
        if (existingEntry && !ignoreCachedEntry) {
            statistics.compileTime = existingEntry.compileTime
            return existingEntry.config
        } else {
            assert configResource.exists(): "Configuration source $configResource.URL does not exist"
            long compileStartTime = System.currentTimeMillis()
            PortletConfig portletConfig = unmarshaller.unmarshal(configResource.inputStream)
            scriptsCompiler.compileAllScripts(portletConfig)
            statistics.compileTime = System.currentTimeMillis() - compileStartTime
            configCache[configResource.URL] = new CacheEntry(statistics.compileTime, portletConfig)
            return portletConfig
        }
    }
}
