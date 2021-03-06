/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.unioninvestment.eai.portal.portlet.crud.domain.model;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import de.unioninvestment.eai.portal.portlet.crud.config.DatabaseQueryConfig;
import org.junit.Test;
import org.mockito.Mock;

import com.vaadin.data.util.sqlcontainer.RowId;
import com.vaadin.data.util.sqlcontainer.TemporaryRowId;
import com.vaadin.data.util.sqlcontainer.query.OrderBy;
import com.vaadin.data.util.sqlcontainer.query.generator.StatementHelper;

import de.unioninvestment.eai.portal.portlet.crud.domain.container.FreeformQueryEventWrapper;
import de.unioninvestment.eai.portal.portlet.crud.domain.database.ConnectionPool;
import de.unioninvestment.eai.portal.portlet.crud.domain.exception.BusinessException;
import de.unioninvestment.eai.portal.portlet.crud.domain.model.DataContainer.ExportWithExportSettings;
import de.unioninvestment.eai.portal.support.vaadin.mvp.EventBus;
import de.unioninvestment.eai.portal.support.vaadin.table.DatabaseQueryDelegate;

public class DatabaseQueryContainerTest
		extends
		AbstractDatabaseContainerTest<DatabaseQueryContainer, SQLContainerEventWrapper> {

	@Mock
	private ConnectionPool connectionPoolMock;

	@Mock
	private FreeformQueryEventWrapper queryDelegateMock;

	@Mock
	private Connection connectionMock;

	@Mock
	private ContainerRowId containerRowIdMock;

	@Mock
	private RowId rowIdMock;

	private List<ContainerOrder> orderBys = new ArrayList<ContainerOrder>();

	@Mock
	private DatabaseQueryDelegate databaseQueryDelegateMock;

	@Mock
	private EventBus eventBus;
    private DatabaseQueryConfig config;

    @Override
	public DatabaseQueryContainer createDataContainer() {
        config = new DatabaseQueryConfig();
        config.setDatasource("eai");

        DatabaseQueryContainer databaseQueryContainer = new DatabaseQueryContainer(
				eventBus, config, "select * from test", true, true, true,
				Arrays.asList("test"), connectionPoolMock, "Benutzer",
				displayPatternMock, orderBys, null, 100, 1000, 0, false);
		databaseQueryContainer.setVaadinContainer(vaadinContainerMock);
		databaseQueryContainer.setQueryDelegate(queryDelegateMock);

		return databaseQueryContainer;
	}

	@Test
	public void shouldAllowAllOperations() {
		assertTrue(container.isInsertable());
		assertTrue(container.isDeleteable());
		assertTrue(container.isUpdateable());
	}

	@Test
	public void shouldAllowOnlyQuerying() {
		DatabaseQueryContainer container = new DatabaseQueryContainer(eventBus,
				config, "select * from test", false, false, false,
				Arrays.asList("test"), connectionPoolMock, "Benutzer",
				displayPatternMock, orderBys, null, 100, 1000, 0, false);
		assertFalse(container.isInsertable());
		assertFalse(container.isDeleteable());
		assertFalse(container.isUpdateable());
	}

	@Test
	public void shouldAllowEmptyPrimaryKeysIfReadonly() {
		new DatabaseQueryContainer(eventBus, config, "select * from test",
				false, false, false, null, connectionPoolMock, "Benutzer",
				displayPatternMock, orderBys, null, 100, 1000, 0, false);
	}

	@Test(expected = BusinessException.class)
	public void shouldRequirePrimaryKeysForEditing() {
		new DatabaseQueryContainer(eventBus, config, "select * from test", true,
				false, false, null, connectionPoolMock, "Benutzer",
				displayPatternMock, orderBys, null, 100, 1000, 0, false);
	}

	@Test
	public void shouldHandleSQLExceptionAsMissingTable()
			throws NamingException, SQLException {
		when(connectionPoolMock.reserveConnection()).thenReturn(connectionMock);
		doThrow(new SQLException("MyMessage")).when(connectionMock)
				.getMetaData();

		try {
			DatabaseQueryContainer container = new DatabaseQueryContainer(
					eventBus, config, "select * from test", true, true, true,
					Arrays.asList("test"), connectionPoolMock, "Benutzer",
					displayPatternMock, orderBys, null, 100, 1000, 0, false);
			container.getVaadinContainer();

			fail("Exception expected");

		} catch (BusinessException e) {
			assertEquals("portlet.crud.error.wrongQuery", e.getCode());
		}
	}

	@Test
	public void shouldHandleRuntimeExceptionAsDataSourceProblem()
			throws NamingException, SQLException {
		when(connectionPoolMock.reserveConnection()).thenReturn(connectionMock);
		doThrow(new RuntimeException("MyMessage")).when(connectionMock)
				.getMetaData();

		try {
			DatabaseQueryContainer container = new DatabaseQueryContainer(
					eventBus, config, "select * from test", true, true, true,
					Arrays.asList("test"), connectionPoolMock, "Benutzer",
					displayPatternMock, orderBys, null, 100, 1000, 0, false);
			container.getVaadinContainer();

		} catch (BusinessException e) {
			assertEquals("portlet.crud.error.wrongQuery", e.getCode());
			assertEquals("eai", e.getArgs()[0]);
		}

	}

	@Test
	public void shouldCommitContainerWithModifiedClob() throws SQLException {
		container.setVaadinContainer(vaadinContainerMock);
		String value = "testValue";
		when(queryDelegateMock.getCLob(any(RowId.class), anyString()))
				.thenReturn(value);
		when(containerRowIdMock.getInternalId()).thenReturn(rowIdMock);
		container.setQueryDelegate(queryDelegateMock);

		ContainerClob containerClob = container.getCLob(containerRowIdMock,
				"TestCol");
		containerClob.setValue("NewValue");

		container.commit();

		verify(vaadinContainerMock).markRowAsModified(rowIdMock);
		assertThat(container.clobFields, is(Collections.<ContainerRowId, Map<String, ContainerClob>>emptyMap()));
	}

	@Test
	public void shouldBuildCLobValueForCLobDBColumn() throws IOException {

		when(queryDelegateMock.getCLob(any(RowId.class), anyString()))
				.thenReturn("TestClob");
		when(containerRowIdMock.getInternalId()).thenReturn(rowIdMock);
		container.setQueryDelegate(queryDelegateMock);
		ContainerClob clob = container.getCLob(containerRowIdMock, "TestCol");

		assertThat(clob.getValue(), is("TestClob"));
	}

	@Test
	public void shouldBuildContainerClobForNewBlobEntry() {
		TemporaryRowId temporaryRowIdMock = mock(TemporaryRowId.class);
		when(containerRowIdMock.getInternalId()).thenReturn(temporaryRowIdMock);

		ContainerClob newClob = container
				.getCLob(containerRowIdMock, "TestCol");

		assertThat(container.isCLobModified(containerRowIdMock, "TestCol"),
				is(false));
		assertThat(newClob.getSize(), is(0));
	}

	@Test
	public void shouldReturnContainerCLobFromCache() {

		String value = "testValue";
		when(queryDelegateMock.getCLob(any(RowId.class), anyString()))
				.thenReturn(value);
		when(containerRowIdMock.getInternalId()).thenReturn(rowIdMock);
		container.setQueryDelegate(queryDelegateMock);

		ContainerClob firstClob = container.getCLob(containerRowIdMock,
				"TestCol");

		ContainerClob secondClob = container.getCLob(containerRowIdMock,
				"TestCol");

		assertThat(firstClob, is(sameInstance(secondClob)));
	}

	@Test
	public void shouldBuildContainerBLobForBLobDBColumn() {

		byte[] value = new byte[] { 1, 2, 4 };
		when(queryDelegateMock.getBLob(any(RowId.class), anyString()))
				.thenReturn(value);
		when(containerRowIdMock.getInternalId()).thenReturn(rowIdMock);
		container.setQueryDelegate(queryDelegateMock);
		ContainerBlob blob = container.getBLob(containerRowIdMock, "TestCol");

		assertThat(blob.getValue(), is(value));
	}

	@Test
	public void shouldReturnContainerBLobFromCache() {

		byte[] value = new byte[] { 1, 2, 4 };
		when(queryDelegateMock.getBLob(any(RowId.class), anyString()))
				.thenReturn(value);
		when(containerRowIdMock.getInternalId()).thenReturn(rowIdMock);
		container.setQueryDelegate(queryDelegateMock);

		ContainerBlob firstBlob = container.getBLob(containerRowIdMock,
				"TestCol");

		ContainerBlob secondBlob = container.getBLob(containerRowIdMock,
				"TestCol");

		assertThat(firstBlob, is(sameInstance(secondBlob)));
	}

	@Test
	public void shouldBuildContainerBlobForNewBlobEntry() {
		TemporaryRowId temporaryRowIdMock = mock(TemporaryRowId.class);
		when(containerRowIdMock.getInternalId()).thenReturn(temporaryRowIdMock);

		ContainerBlob newBlob = container
				.getBLob(containerRowIdMock, "TestCol");

		assertThat(container.isBLobModified(containerRowIdMock, "TestCol"),
				is(false));
		assertThat(newBlob.isEmpty(), is(true));
	}

	@Test
	public void shouldCheckIfBlobDBColumnIsEmpty() {
		when(queryDelegateMock.hasBlobData(any(RowId.class), anyString()))
				.thenReturn(false);

		boolean isEmpty = container.isBLobEmpty(containerRowIdMock, "TestCol");
		assertThat(isEmpty, is(false));
	}

	@Test
	public void shouldCommitContainerWithModifiedBlob() throws SQLException {
		container.setVaadinContainer(vaadinContainerMock);
		byte[] value = new byte[] { 1, 2, 3 };
		byte[] newValue = new byte[] { 1, 2, 3, 4, 5 };
		when(queryDelegateMock.getBLob(any(RowId.class), anyString()))
				.thenReturn(value);
		when(containerRowIdMock.getInternalId()).thenReturn(rowIdMock);
		container.setQueryDelegate(queryDelegateMock);

		ContainerBlob containerBlob = container.getBLob(containerRowIdMock,
				"TestCol");
		containerBlob.setValue(newValue);

		container.commit();

		verify(vaadinContainerMock).markRowAsModified(rowIdMock);
		assertThat(container.blobFields, is(Collections.<ContainerRowId, Map<String, ContainerBlob>>emptyMap()));
	}

	@Override
	public SQLContainerEventWrapper createVaadinContainer() {

		return mock(SQLContainerEventWrapper.class, withSettings()
				.extraInterfaces(Filterable.class));
	}

	@Test
	public void shouldChangePageLengthOnExport() {
		container.withExportSettings(new ExportWithExportSettings() {
			@Override
			public void export() {
				verify(vaadinContainerMock).setPageLength(1000);
			}
		});
		verify(vaadinContainerMock).setPageLength(100);
	}

	@Test
	public void shouldReturnTheCurrentQueryFromTheBackend() {
		container.setDatabaseQueryDelegate(databaseQueryDelegateMock);
		StatementHelper helper = new StatementHelper();
		when(databaseQueryDelegateMock.getQueryStatement(0, 0)).thenReturn(
				helper);

		assertThat(container.getCurrentQuery(true), is(helper));
	}

	@Test
	public void shouldRemoveTheOrderingFromTheBackend() {
		List<OrderBy> previousOrderBys = asList(new OrderBy("A", true));
		when(databaseQueryDelegateMock.getOrderBy()).thenReturn(
				previousOrderBys);

		container.setDatabaseQueryDelegate(databaseQueryDelegateMock);
		StatementHelper helper = new StatementHelper();
		when(databaseQueryDelegateMock.getQueryStatement(0, 0)).thenReturn(
				helper);

		assertThat(container.getCurrentQuery(false), is(helper));
		verify(databaseQueryDelegateMock).setOrderBy(previousOrderBys);
	}

    @Test
    public void noInfoForTypeMessage(){
        String message = container.getNoTypeInformationForColumnMessage("col1");
        assertThat(message, equalTo("Could not retrieve type information for column 'col1' from query 'select * from test'. Does it exist in the backend?"));

    }



}
