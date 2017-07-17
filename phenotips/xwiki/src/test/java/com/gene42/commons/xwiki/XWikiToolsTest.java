/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.xwiki;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.users.User;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiGroupService;

import net.jcip.annotations.NotThreadSafe;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for XWikiTools.
 *
 * @version $Id$
 */
@NotThreadSafe
public class XWikiToolsTest
{
    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Running : " + description.getMethodName());
        }
    };

    @Rule
    public final MockitoComponentMockingRule<XWikiTools> mocker =
        new MockitoComponentMockingRule<>(XWikiTools.class);

    @Mock
    private Provider<XWikiContext> contextProvider;

    private XWikiGroupService groupService = new GroupServiceStub();
    private TestUser bob;

    public XWikiToolsTest() throws Exception
    {

    }

    /**
     * Class set up.
     *
     * @throws  Exception  on error
     */
    @BeforeClass
    public static void setUpClass() throws Exception {

    }

    /**
     * Class tear down.
     *
     * @throws  Exception  on error
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test set up.
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        XWiki wiki = mock(XWiki.class);
        XWikiContext context = mock(XWikiContext.class);

        when(wiki.getGroupService(any(XWikiContext.class))).thenReturn(this.groupService);
        when(context.getWiki()).thenReturn(wiki);

        Provider<XWikiContext> provider = this.mocker.getInstance(XWikiContext.TYPE_PROVIDER);

        when(provider.get()).thenReturn(context);

        TestUser.clearUsers();
        Group.clearGroups();

        this.bob = TestUser.createUser("Bob");
        Group.addGroup("GroupA");
        Group.addGroup("GroupB");
        Group.addGroup("GroupC");
    }

    /**
     * Test tear down.
     */
    @After
    public void tearDown() {
    }

    @Test
    public void getGroupsUserBelongsToMultipleGroupsTest() throws Exception
    {

    }

    @Test
    public void getGroupsUserBelongsToNestedGroupsTest() throws Exception
    {
        Group.addGroup("GroupZ");
        Group.addGroup("GroupY");
        Group.addGroup("GroupM");
        Group.addGroup("GroupN");
        Group.addToGroup("GroupB", "GroupA");
        Group.addToGroup("GroupB", "GroupC");
        Group.addToGroup("GroupZ", "GroupY");
        Group.addToGroup("GroupC", "GroupZ");

        Group.addToGroup("GroupM", "GroupN");
        Group.addToGroup("GroupM", "GroupB");

        TestUser.addToGroup("Bob", "GroupB");


        Set<String> groups = this.mocker.getComponentUnderTest().getGroupsUserBelongsTo(this.bob);

        //System.out.println("result  : " + Arrays.toString(groups.toArray()));

        Collection<String> expected = Arrays.asList(
            getGroupName("GroupA"), getGroupName("GroupB"),
            getGroupName("GroupC"), getGroupName("GroupZ"), getGroupName("GroupY"));

        //System.out.println("expected: " + Arrays.toString(expected.toArray()));

        assertTrue(CollectionUtils.isNotEmpty(groups));
        assertTrue(CollectionUtils.containsAll(expected, groups));
    }

    @Test
    public void getGroupsUserBelongsToCircularGroupsTest() throws Exception
    {
        Group.addToGroup("GroupB", "GroupA");
        Group.addToGroup("GroupC", "GroupB");
        Group.addToGroup("GroupA", "GroupC");

        TestUser.addToGroup("Bob", "GroupA");

        Collection<String> expected = Arrays.asList(
            getGroupName("GroupA"), getGroupName("GroupB"), getGroupName("GroupC"));

        Set<String> groups = this.mocker.getComponentUnderTest().getGroupsUserBelongsTo(this.bob);

        //System.out.println("result  : " + Arrays.toString(groups.toArray()));

        assertTrue(CollectionUtils.isNotEmpty(groups));
        assertTrue(CollectionUtils.containsAll(expected, groups));
    }

    @Test
    public void getGroupsUserBelongsToNoGroupsTest() throws Exception
    {
        Group.addToGroup("GroupB", "GroupA");

        Set<String> groups = this.mocker.getComponentUnderTest().getGroupsUserBelongsTo(this.bob);
        assertTrue(CollectionUtils.isEmpty(groups));
    }

    private static String getGroupName(String name)
    {
        return "xwiki:Groups." + name;
    }

    private static class TestUser extends Group implements User
    {
        static Map<String, TestUser> MAP = new HashMap<>();

        static TestUser createUser(String name)
        {
            TestUser user = new TestUser();
            user.name = name;
            MAP.put(name, user);
            return user;
        }

        static TestUser getUser(String name)
        {
            return MAP.get(name);
        }

        static boolean doesGroupHaveUser(Group group, String userName)
        {
            return group.children.contains(userName);
        }

        static Collection<DocumentReference> getDocRefs(String userName)
        {
            Collection<DocumentReference> result = new LinkedList<>();
            for (Group group : Group.MAP.values()) {
                if (doesGroupHaveUser(group, userName)) {
                    DocumentReference docRef = new DocumentReference("xwiki", "Groups", group.name);
                    result.add(docRef);
                }
            }
            return result;
        }

        static void clearUsers()
        {
            MAP.values().forEach(Group::clearGroup);
            MAP.clear();
        }

        @Override
        public boolean exists()
        {
            return true;
        }

        @Override
        public String getId()
        {
            return this.name;
        }

        @Override
        public String getUsername()
        {
            return this.name;
        }

        @Override
        public String getName()
        {
            return this.name;
        }

        @Override
        public DocumentReference getProfileDocument()
        {
            return new DocumentReference("xwiki", "XWiki", this.name);
        }

        @Override
        public URI getProfileURI()
        {
            return null;
        }

        @Override
        public Object getAttribute(String s)
        {
            return null;
        }

        @Override
        public int compareTo(User user)
        {
            return this.name.compareTo(user.getId());
        }
    }

    private static class Group
    {
        static Map<String, Group> MAP = new HashMap<>();

        String name;
        Set<String> children = new HashSet<>();

        static void addGroup(String name)
        {
            Group group = new Group();
            group.name = name;
            MAP.put(name, group);
        }

        static void addToGroup(String name, String nameOfGroupToAddTo)
        {
            MAP.get(nameOfGroupToAddTo).children.add(name);
        }

        static void clearGroup(Group group)
        {
            group.children.clear();
            group.name = null;
        }

        static void clearGroups()
        {
            MAP.values().forEach(Group::clearGroup);
            MAP.clear();
        }
    }


    private static class GroupServiceStub implements XWikiGroupService
    {
        @Override public void init(XWiki xWiki, XWikiContext xWikiContext) throws XWikiException
        {

        }

        @Override public void initCache(XWikiContext xWikiContext) throws XWikiException
        {

        }

        @Override public void initCache(int i, XWikiContext xWikiContext) throws XWikiException
        {

        }

        @Override public void flushCache()
        {

        }

        @Override public Collection<String> listGroupsForUser(String s, XWikiContext xWikiContext) throws XWikiException
        {
            return null;
        }

        @Override public void addUserToGroup(String s, String s1, String s2, XWikiContext xWikiContext)
            throws XWikiException
        {

        }

        @Override public void removeUserOrGroupFromAllGroups(String s, String s1, String s2, XWikiContext xWikiContext)
            throws XWikiException
        {

        }

        @Override public List<String> listMemberForGroup(String s, XWikiContext xWikiContext) throws XWikiException
        {
            return null;
        }

        @Override public List<String> listAllGroups(XWikiContext xWikiContext) throws XWikiException
        {
            return null;
        }

        @Override public List<?> getAllMatchedUsers(Object[][] objects, boolean b, int i, int i1, Object[][] objects1,
            XWikiContext xWikiContext) throws XWikiException
        {
            return null;
        }

        @Override public List<?> getAllMatchedGroups(Object[][] objects, boolean b, int i, int i1, Object[][] objects1,
            XWikiContext xWikiContext) throws XWikiException
        {
            return null;
        }

        @Override public int countAllMatchedUsers(Object[][] objects, XWikiContext xWikiContext) throws XWikiException
        {
            return 0;
        }

        @Override public int countAllMatchedGroups(Object[][] objects, XWikiContext xWikiContext) throws XWikiException
        {
            return 0;
        }

        @Override
        public Collection<String> getAllGroupsNamesForMember(String s, int i, int i1, XWikiContext xWikiContext)
            throws XWikiException
        {
            return null;
        }

        @Override
        public Collection<DocumentReference> getAllGroupsReferencesForMember(DocumentReference user, int var2, int
            var3, XWikiContext var4) throws
            XWikiException
        {
            String name = user.toString();
            String [] tokens = StringUtils.split(name, ".");
            return TestUser.getDocRefs(tokens[1]);
        }

        @Override
        public Collection<String> getAllMembersNamesForGroup(String s, int i, int i1, XWikiContext xWikiContext)
            throws XWikiException
        {
            return null;
        }

        @Override
        public Collection<String> getAllMatchedMembersNamesForGroup(String s, String s1, int i, int i1,
            Boolean aBoolean,
            XWikiContext xWikiContext) throws XWikiException
        {
            return null;
        }

        @Override public int countAllGroupsNamesForMember(String s, XWikiContext xWikiContext) throws XWikiException
        {
            return 0;
        }

        @Override public int countAllMembersNamesForGroup(String s, XWikiContext xWikiContext) throws XWikiException
        {
            return 0;
        }
    }
}
