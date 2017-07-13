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
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.user.api.XWikiGroupService;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for XWikiTools.
 *
 * @version $Id$
 */
public class XWikiToolsTest
{
    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Running test: " + description.getMethodName());
        }
    };

    @Rule
    public final MockitoComponentMockingRule<XWikiTools> mocker =
        new MockitoComponentMockingRule<>(XWikiTools.class);

    //@Mock
    private XWiki wiki;

    //@Mock
    private XWikiContext context;

    @Mock
    private Provider<XWikiContext> contextProvider;



    //@Mock
    private XWikiGroupService groupService;
    //private XWikiTools xWikiTools;

    //@Mock
    //private XWiki wiki;

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

        this.wiki = mock(XWiki.class);
        this.context = mock(XWikiContext.class);

        this.groupService = mock(XWikiGroupService.class);
        when(this.wiki.getGroupService(any(XWikiContext.class))).thenReturn(this.groupService);
        when(this.context.getWiki()).thenReturn(this.wiki);
        when(this.contextProvider.get()).thenReturn(this.context);

        TestUser.MAP.clear();
        Group.MAP.clear();
    }

    /**
     * Test tear down.
     */
    @After
    public void tearDown() {
    }

    @Ignore
    @Test
    public void test1() throws Exception
    {
        //DocumentReference var1, int var2, int var3, XWikiContext var4
        TestUser user = TestUser.createUser("Bob");

        DocumentReference userDoc = new DocumentReference("xwiki", "XWiki", "Bob");

        when(this.groupService.getAllGroupsReferencesForMember(userDoc, 1000, 0, this.context)).thenReturn(TestUser.getDocRefs(user));

        Set<String> groups = this.mocker.getComponentUnderTest().getGroupsUserBelongsTo(user);

        System.out.println(Arrays.toString(groups.toArray()));
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

        static boolean doesGroupHaveUser(Group group, TestUser user)
        {
            return group.children.contains(user.name);
        }

        static Collection<DocumentReference> getDocRefs(TestUser user)
        {
            Collection<DocumentReference> result = new LinkedList<>();
            for (Group group : MAP.values()) {
                if (doesGroupHaveUser(group, user)) {
                    DocumentReference docRef = new DocumentReference("xwiki", "Groups", group.name);
                    result.add(docRef);
                }
            }
            return result;
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

        static void addToGroup(String name, String nameOfGroupToAddTo)
        {
            MAP.get(nameOfGroupToAddTo).children.add(name);
        }
    }
}
