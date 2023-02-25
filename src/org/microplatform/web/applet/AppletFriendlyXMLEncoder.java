package org.microplatform.web.applet;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.BeanInfo;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.EventSetDescriptor;
import java.beans.Expression;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PersistenceDelegate;
import java.beans.PropertyDescriptor;
import java.beans.Statement;
import java.beans.XMLEncoder;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.undo.UndoableEditSupport;

/**
 * We set persistence delegates where the no argument constructor can't be used
 * Warning, the java bug #4741757 is really annoying there when we want to
 * deploy as unsigned applet or web start app because it breaks the security
 * sandbox. We work arround that bug by taking inspiration from the great Violet
 * UML code form Cay Horstmann TODO: use a simpler solution (new
 * DefaultPersistenceDelegate(new String[]...)) when Java mustang will be the
 * norm or when the fix is retrofited to most of clients.
 * 
 * @author Raphael Valyi (with parts copied from Sun).
 * 
 */
public class AppletFriendlyXMLEncoder extends XMLEncoder {

	protected static AppletFriendlyPersistenceDelegate delegate1 = new AppletFriendlyPersistenceDelegate();

	protected static AppletFriendlyDefaultPersistenceDelegate delegate2 = new AppletFriendlyDefaultPersistenceDelegate();

	public AppletFriendlyXMLEncoder(OutputStream out) {
		super(out);
	}

	public static Class typeToClass(Class type) {
		return type;// TODO: the following is the code Sun code for jre > 1.4. I
		// don't know what is the benefit...
		/*
		 * return type.isPrimitive() ? ObjectHandler.typeNameToClass(type
		 * .getName()) : type;
		 */
	}

	static boolean equals(Object o1, Object o2) {
		return (o1 == null) ? (o2 == null) : o1.equals(o2);
	}

	/**
	 * Should you have some persistence delegates, you can register them inside
	 * that method But warning, the bug #4741757 is lurking behind you.
	 */
	public PersistenceDelegate getPersistenceDelegate(Class type) {

		// will manage quickly with the super class without requesting any
		// BeanInfo:
		if (type == null)
			return super.getPersistenceDelegate(type);
		if (type.isArray())
			return super.getPersistenceDelegate(type);
		if (isPrimitive(type))
			return super.getPersistenceDelegate(type);
		if (type == Object.class)
			return delegate1;

		// Now there are some common types we know we will need a persistence
		// delegate
		// those are present in java.beans.Metada (f#ck!) but aren't public so
		// we copied them
		// here again:
		if (type == String.class)
			return new java_lang_String_PersistenceDelegate();
		if (type == Class.class)
			return new java_lang_Class_PersistenceDelegate();
		if (type == Map.class || type == AbstractMap.class
			|| type == Hashtable.class) //TODO this won't handle the cell
			// user object properly, why??
			return new java_util_Map_PersistenceDelegate();
		if (type == List.class || type == AbstractList.class)
			return new java_util_List_PersistenceDelegate();

		if (type == Collection.class || type == AbstractCollection.class
				|| type == HashSet.class || type == AbstractSet.class
				|| type == TreeSet.class)// sure?
			return new java_util_Collection_PersistenceDelegate();
		if (type == DefaultMutableTreeNode.class)
			return new javax_swing_tree_DefaultMutableTreeNode_PersistenceDelegate();
		if (type == Color.class)
			return new java_awt_Color_PersistenceDelegate();
		if (type == Point2D.Double.class)
			return new java_awt_geom_Point2D$Double_PersistenceDelegate();
		if (type == Rectangle2D.class)
			return new java_awt_geom_Rectangle2D_PersistenceDelegate();
		if (type == Font.class)
			return new java_awt_Font_PersistenceDelegate();

		if (isJRETypeOKWithDefaultDelegate(type))// OK, a JRE type that use
			// to work with the
			// DefaultPersistenceDelegat
			// from sun
			return delegate1;

		// now that the boring part: the BeanInfo of the following will be
		// requested
		// they are two ways of avoiding it that: lazily load the BeanInfo you
		// are
		// likely to encounter during the Startup or provide your own delegate
		// earlier.
		if (type.getName().indexOf(".util") > 0)// almost all collections have
			// specialized persistence
			// delegates
			return super.getPersistenceDelegate(type);
		if (type.getName().indexOf(".awt") > 0)// you could try to provide your
			// own delegates for awt as they
			// are few
			return super.getPersistenceDelegate(type);
		if (type.getName().indexOf(".swing") > 0)// swing hardely complies
			// with
			// DefaultPersistenceDelegate
			return super.getPersistenceDelegate(type);
		if (type.getName().indexOf(".beans") > 0)// hopefully you won't have
			// many
			return super.getPersistenceDelegate(type);
		if (type.getName().indexOf(".reflect") > 0)// hopefully you won't have
			// many
			return super.getPersistenceDelegate(type);

		// else our smart persistence delegate will avoid the BeanInfo lookup
		return delegate1;
	}

	/**
	 * Hook for subclassers: according to the types you may encounter in the
	 * bean you want to serialize, you may subclass it and tells that other
	 * types from the JRE can do well with the
	 * AppletFriendlyPersistenceDelegate. Basically the fastest way to know that
	 * is to test if it works for your type (it will work every time
	 * java.beans.DefaultPersistenceDelagate would have worked).
	 * 
	 * @param type
	 */
	protected boolean isJRETypeOKWithDefaultDelegate(Class type) {
		if (type == UndoableEditSupport.class || type == Dictionary.class
				|| type == AbstractSet.class || type == Point2D.class
				|| type == Rectangle2D.class || type == ArrayList.class
				|| type == Point2D.class || type == Rectangle2D.class
				|| type == Object.class || type == ArrayList.class)
			return true;
		return false;
	}

	/**
	 * Hook for subclassers. This where to put custom delegate for types for
	 * your won hierarchy if they don't respect the javabeans conventions and
	 * can't be serialized using the DefaultPersistenceDelegate as such.
	 * 
	 * @param type
	 * @return
	 */
	protected PersistenceDelegate getDelegateForOurType(Class type) {
		return delegate1;// by default we respect javabeans conventions for
		// our types and we don't need any custom delegate.
	}

	/**
	 * Our DefaultPersistenceDelegate delegate
	 * 
	 * @author rvalyi
	 */
	public static class AppletFriendlyDefaultPersistenceDelegate extends
			DefaultPersistenceDelegate {

		public Expression instantiateBreaker(Object oldInstance, Encoder out) {
			return this.instantiate(oldInstance, out);
		}

		protected boolean mutatesToBreaker(Object oldInstance,
				Object newInstance) {
			return this.mutatesTo(oldInstance, newInstance);
		}

		protected void initialize(Class type, Object oldInstance,
				Object newInstance, Encoder out) {
			delegate1.initialize(type, oldInstance, newInstance, out);
		}

		protected void initializeBreaker(Class type, Object oldInstance,
				Object newInstance, Encoder out) {
			super.initialize(type, oldInstance, newInstance, out);
		}
		
		static void invokeStatementAppletFirendly(Object instance, String methodName,
				Object[] args, Encoder out) {
			out.writeStatement(new Statement(instance, methodName, args));
		}
	}

	/**
	 * We can't subclass only the DefaultPersistenceDelegatev because we will
	 * need to access the initialisze method of PersistenceDelegate and this
	 * method is protected. We would then have to acces it via super.initialize,
	 * but we don't want that because it would force the BeanInfo lookup in the
	 * DefaultPersistenceDelegate class! On the contrary, if we decide to
	 * subclass only PersistenceDelegate, then we still want some of the
	 * conveninent default method of the defaultPersistenceDelegate, so we use a
	 * defaultPersistenceDelegate delegate (yes, I said that!) to do that.
	 * 
	 * @author Raphael Valyi
	 */
	public static class AppletFriendlyPersistenceDelegate extends
			PersistenceDelegate {

		protected Expression instantiate(Object oldInstance, Encoder out) {
			return delegate2.instantiateBreaker(oldInstance, out);
		}

		protected boolean mutatesTo(Object oldInstance, Object newInstance) {
			return delegate2.mutatesToBreaker(oldInstance, newInstance);
		}

		public void writeObject(Object oldInstance, Encoder out) {
			delegate2.writeObject(oldInstance, out);
		}

		/**
		 * Conform copy of java.beans.DefaultPersistenceDelegate's method
		 */
		public void initialize(Class type, Object oldInstance,
				Object newInstance, Encoder out) {
			super.initialize(type, oldInstance, newInstance, out);
			if (oldInstance.getClass() == type) {
				initBean(type, oldInstance, newInstance, out);
			}
		}

		/**
		 * Conform copy of java.beans.DefaultPersistenceDelegate's method
		 * 
		 * @param type
		 * @param oldInstance
		 * @param newInstance
		 * @param out
		 */
		public void initializeBreaker(Class type, Object oldInstance,
				Object newInstance, Encoder out) {
			this.initialize(type, oldInstance, newInstance, out);
		}

		/**
		 * Conform copy of java.beans.DefaultPersistenceDelegate's method
		 * 
		 * @param instance
		 * @param methodName
		 * @param args
		 * @param out
		 */
		static void invokeStatement(Object instance, String methodName,
				Object[] args, Encoder out) {
			out.writeStatement(new Statement(instance, methodName, args));
		}

		/**
		 * Conform copy of java.beans.DefaultPersistenceDelegate's method except
		 * we use the IGNORE_ALL_BEANINFO flag
		 * 
		 * @param type
		 * @param oldInstance
		 * @param newInstance
		 * @param out
		 */
		private void initBean(Class type, Object oldInstance,
				Object newInstance, Encoder out) {
			// System.out.println("initBean: " + oldInstance);
			BeanInfo info = null;
			try {
				info = Introspector.getBeanInfo(type,
						Introspector.IGNORE_ALL_BEANINFO);
			} catch (IntrospectionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return;
			}

			// Properties
			PropertyDescriptor[] propertyDescriptors = info
					.getPropertyDescriptors();
			for (int i = 0; i < propertyDescriptors.length; ++i) {
				try {
					doProperty(type, propertyDescriptors[i], oldInstance,
							newInstance, out);
				} catch (Exception e) {
					out.getExceptionListener().exceptionThrown(e);
				}
			}

			if (!java.awt.Component.class.isAssignableFrom(type)) {
				return; // Just handle the listeners of Components for now.
			}
			EventSetDescriptor[] eventSetDescriptors = info
					.getEventSetDescriptors();
			for (int e = 0; e < eventSetDescriptors.length; e++) {
				EventSetDescriptor d = eventSetDescriptors[e];
				Class listenerType = d.getListenerType();

				// The ComponentListener is added automatically, when
				// Contatiner:add is called on the parent.
				if (listenerType == java.awt.event.ComponentListener.class) {
					continue;
				}

				// JMenuItems have a change listener added to them in
				// their "add" methods to enable accessibility support -
				// see the add method in JMenuItem for details. We cannot
				// instantiate this instance as it is a private inner class
				// and do not need to do this anyway since it will be created
				// and installed by the "add" method. Special case this for now,
				// ignoring all change listeners on JMenuItems.
				if (listenerType == javax.swing.event.ChangeListener.class
						&& type == javax.swing.JMenuItem.class) {
					continue;
				}

				EventListener[] oldL = new EventListener[0];
				EventListener[] newL = new EventListener[0];
				try {
					Method m = d.getGetListenerMethod();
					oldL = (EventListener[]) m.invoke(oldInstance,
							new Object[] {});
					newL = (EventListener[]) m.invoke(newInstance,
							new Object[] {});
				} catch (Throwable e2) {
					try {
						Method m = type.getMethod("getListeners",
								new Class[] { Class.class });
						oldL = (EventListener[]) m.invoke(oldInstance,
								new Object[] { listenerType });
						newL = (EventListener[]) m.invoke(newInstance,
								new Object[] { listenerType });
					} catch (Exception e3) {
						return;
					}
				}

				// Asssume the listeners are in the same order and that there
				// are no gaps.
				// Eventually, this may need to do true differencing.
				String addListenerMethodName = d.getAddListenerMethod()
						.getName();
				for (int i = newL.length; i < oldL.length; i++) {
					// System.out.println("Adding listener: " +
					// addListenerMethodName + oldL[i]);
					invokeStatement(oldInstance, addListenerMethodName,
							new Object[] { oldL[i] }, out);
				}

				String removeListenerMethodName = d.getRemoveListenerMethod()
						.getName();
				for (int i = oldL.length; i < newL.length; i++) {
					invokeStatement(oldInstance, removeListenerMethodName,
							new Object[] { oldL[i] }, out);
				}
			}
		}

		/**
		 * Conform copy of java.beans.DefaultPersistenceDelegate's method
		 * 
		 * @param type
		 * @param pd
		 * @param oldInstance
		 * @param newInstance
		 * @param out
		 * @throws Exception
		 */
		private void doProperty(Class type, PropertyDescriptor pd,
				Object oldInstance, Object newInstance, Encoder out)
				throws Exception {
			Method getter = pd.getReadMethod();
			Method setter = pd.getWriteMethod();

			if (getter != null && setter != null && !isTransient(type, pd)) {
				Expression oldGetExp = new Expression(oldInstance, getter
						.getName(), new Object[] {});
				Expression newGetExp = new Expression(newInstance, getter
						.getName(), new Object[] {});
				Object oldValue = oldGetExp.getValue();
				Object newValue = newGetExp.getValue();
				out.writeExpression(oldGetExp);
				if (!AppletFriendlyXMLEncoder.equals(newValue, out
						.get(oldValue))) {
					// Search for a static constant with this value;
					Object e = pd.getValue("enumerationValues");
					if (e instanceof Object[] && Array.getLength(e) % 3 == 0) {
						Object[] a = (Object[]) e;
						for (int i = 0; i < a.length; i = i + 3) {
							try {
								Field f = type.getField((String) a[i]);
								if (f.get(null).equals(oldValue)) {
									out.remove(oldValue);
									out.writeExpression(new Expression(
											oldValue, f, "get",
											new Object[] { null }));
								}
							} catch (Exception ex) {
							}
						}
					}
					invokeStatement(oldInstance, setter.getName(),
							new Object[] { oldValue }, out);
				}
			}
		}

		/**
		 * Conform copy of java.beans.DefaultPersistenceDelegate's method except
		 * we use the IGNORE_ALL_BEANINFO flag
		 * 
		 * @param type
		 * @param pd
		 * @return
		 */
		private boolean isTransient(Class type, PropertyDescriptor pd) {
			if (type == null) {
				return false;
			}
			String pName = pd.getName();
			BeanInfo info = null;
			try {
				info = Introspector.getBeanInfo(type,
						Introspector.IGNORE_ALL_BEANINFO);
			} catch (IntrospectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			PropertyDescriptor[] propertyDescriptors = info
					.getPropertyDescriptors();
			for (int i = 0; i < propertyDescriptors.length; ++i) {
				PropertyDescriptor pd2 = propertyDescriptors[i];
				if (pName.equals(pd2.getName())) {
					Object value = pd2.getValue("transient");
					if (value != null) {
						return Boolean.TRUE.equals(value);
					}
				}
			}
			return isTransient(type.getSuperclass(), pd);
		}

	}

	public static boolean isPrimitive(Class type) {
		return primitiveTypeFor(type) != null;
	}

	public static Class primitiveTypeFor(Class wrapper) {
		if (wrapper == Boolean.class)
			return Boolean.TYPE;
		if (wrapper == Byte.class)
			return Byte.TYPE;
		if (wrapper == Character.class)
			return Character.TYPE;
		if (wrapper == Short.class)
			return Short.TYPE;
		if (wrapper == Integer.class)
			return Integer.TYPE;
		if (wrapper == Long.class)
			return Long.TYPE;
		if (wrapper == Float.class)
			return Float.TYPE;
		if (wrapper == Double.class)
			return Double.TYPE;
		if (wrapper == Void.class)
			return Void.TYPE;
		return null;
	}

	public static class java_lang_String_PersistenceDelegate extends
			PersistenceDelegate {
		protected Expression instantiate(Object oldInstance, Encoder out) {
			return null;
		}

		public void writeObject(Object oldInstance, Encoder out) {
			// System.out.println("NullPersistenceDelegate:writeObject " +
			// oldInstance);
		}
	}

	public static class java_lang_Class_PersistenceDelegate extends
			PersistenceDelegate {
		protected Expression instantiate(Object oldInstance, Encoder out) {
			Class c = (Class) oldInstance;
			// As of 1.3 it is not possible to call Class.forName("int"),
			// so we have to generate different code for primitive types.
			// This is needed for arrays whose subtype may be primitive.
			if (c.isPrimitive()) {
				Field field = null;
				try {
					field = typeToClass(c).getDeclaredField("TYPE");
				} catch (NoSuchFieldException ex) {
					System.err.println("Unknown primitive type: " + c);
				}
				return new Expression(oldInstance, field, "get",
						new Object[] { null });
			} else if (oldInstance == String.class) {
				return new Expression(oldInstance, "", "getClass",
						new Object[] {});
			} else if (oldInstance == Class.class) {
				return new Expression(oldInstance, String.class, "getClass",
						new Object[] {});
			} else {
				return new Expression(oldInstance, Class.class, "forName",
						new Object[] { c.getName() });
			}
		}
	}

	public static class java_util_Map_PersistenceDelegate extends
			AppletFriendlyDefaultPersistenceDelegate {

		public void initialize(Class type, Object oldInstance,
				Object newInstance, Encoder out) {
			// System.out.println("Initializing: " + newInstance);
			java.util.Map oldMap = (java.util.Map) oldInstance;
			java.util.Map newMap = (java.util.Map) newInstance;
			// Remove the new elements.
			// Do this first otherwise we undo the adding work.
			if (newMap != null) {
				java.util.Iterator newKeys = newMap.keySet().iterator();
				while (newKeys.hasNext()) {
					Object newKey = newKeys.next();
					// PENDING: This "key" is not in the right environment.
					if (!oldMap.containsKey(newKey)) {
						invokeStatementAppletFirendly(oldInstance, "remove",
								new Object[] { newKey }, out);
					}
				}
			}
			// Add the new elements.
			java.util.Iterator oldKeys = oldMap.keySet().iterator();
			while (oldKeys.hasNext()) {
				Object oldKey = oldKeys.next();

				Expression oldGetExp = new Expression(oldInstance, "get",
						new Object[] { oldKey });
				// Pending: should use newKey.
				Expression newGetExp = new Expression(newInstance, "get",
						new Object[] { oldKey });
				try {
					Object oldValue = oldGetExp.getValue();
					Object newValue = newGetExp.getValue();
					out.writeExpression(oldGetExp);
					if (!AppletFriendlyXMLEncoder.equals(newValue, out
							.get(oldValue))) {
						invokeStatementAppletFirendly(oldInstance, "put", new Object[] {
								oldKey, oldValue }, out);
					}
				} catch (Exception e) {
					out.getExceptionListener().exceptionThrown(e);
				}
			}
		}

		/*
		 * public void initialize(Class type, Object oldInstance, Object
		 * newInstance, Encoder out) { // System.out.println("Initializing: " +
		 * newInstance); java.util.Map oldMap = (java.util.Map) oldInstance;
		 * java.util.Map newMap = (java.util.Map) newInstance; // Remove the new
		 * elements. // Do this first otherwise we undo the adding work. if
		 * (newMap != null) { Object newKey; Iterator it =
		 * newMap.keySet().iterator(); while (it.hasNext()) { newKey =
		 * it.next(); // PENDING: This "key" is not in the right environment. if
		 * (!oldMap.containsKey(newKey)) { invokeStatement(oldInstance,
		 * "remove", new Object[] { newKey }, out); } } } // Add the new
		 * elements. Object oldKey; Iterator it = oldMap.keySet().iterator();
		 * while (it.hasNext()) { oldKey = it.next(); Expression oldGetExp = new
		 * Expression(oldInstance, "get", new Object[] { oldKey }); // Pending:
		 * should use newKey. Expression newGetExp = new Expression(newInstance,
		 * "get", new Object[] { oldKey }); try { Object oldValue =
		 * oldGetExp.getValue(); Object newValue = newGetExp.getValue();
		 * out.writeExpression(oldGetExp); if
		 * (!AppletFriendlyXMLEncoder.equals(newValue, out .get(oldValue))) {
		 * invokeStatement(oldInstance, "put", new Object[] { oldKey, oldValue },
		 * out); } else if ((newValue == null) && !newMap.containsKey(oldKey)) { //
		 * put oldValue(=null?) if oldKey is absent in newMap
		 * invokeStatement(oldInstance, "put", new Object[] { oldKey, oldValue },
		 * out); } } catch (Exception e) {
		 * out.getExceptionListener().exceptionThrown(e); } } }
		 */
	}

	public static class java_util_List_PersistenceDelegate extends
			AppletFriendlyDefaultPersistenceDelegate {
		public void initialize(Class type, Object oldInstance,
				Object newInstance, Encoder out) {
			java.util.List oldO = (java.util.List) oldInstance;
			java.util.List newO = (java.util.List) newInstance;
			int oldSize = oldO.size();
			int newSize = (newO == null) ? 0 : newO.size();
			if (oldSize < newSize) {
				invokeStatementAppletFirendly(oldInstance, "clear", new Object[] {}, out);
				newSize = 0;
			}
			for (int i = 0; i < newSize; i++) {
				Object index = new Integer(i);

				Expression oldGetExp = new Expression(oldInstance, "get",
						new Object[] { index });
				Expression newGetExp = new Expression(newInstance, "get",
						new Object[] { index });
				try {
					Object oldValue = oldGetExp.getValue();
					Object newValue = newGetExp.getValue();
					out.writeExpression(oldGetExp);
					if (!AppletFriendlyXMLEncoder.equals(newValue, out
							.get(oldValue))) {
						invokeStatementAppletFirendly(oldInstance, "set", new Object[] {
								index, oldValue }, out);
					}
				} catch (Exception e) {
					out.getExceptionListener().exceptionThrown(e);
				}
			}
			for (int i = newSize; i < oldSize; i++) {
				invokeStatementAppletFirendly(oldInstance, "add",
						new Object[] { oldO.get(i) }, out);
			}
		}
	}

	public static class java_util_Collection_PersistenceDelegate extends
	AppletFriendlyDefaultPersistenceDelegate {
		public void initialize(Class type, Object oldInstance,
				Object newInstance, Encoder out) {
			java.util.Collection oldO = (java.util.Collection) oldInstance;
			java.util.Collection newO = (java.util.Collection) newInstance;

			if (newO.size() != 0) {
				invokeStatementAppletFirendly(oldInstance, "clear", new Object[] {}, out);
			}
			for (Iterator i = oldO.iterator(); i.hasNext();) {
				invokeStatementAppletFirendly(oldInstance, "add", new Object[] { i.next() },
						out);
			}
		}
	}

	public static class javax_swing_tree_DefaultMutableTreeNode_PersistenceDelegate
			extends AppletFriendlyDefaultPersistenceDelegate {
		public void initialize(Class type, Object oldInstance,
				Object newInstance, Encoder out) {
			super.initialize(type, oldInstance, newInstance, out);
			javax.swing.tree.DefaultMutableTreeNode m = (javax.swing.tree.DefaultMutableTreeNode) oldInstance;
			javax.swing.tree.DefaultMutableTreeNode n = (javax.swing.tree.DefaultMutableTreeNode) newInstance;
			for (int i = n.getChildCount(); i < m.getChildCount(); i++) {
				invokeStatementAppletFirendly(oldInstance, "add", new Object[] { m
						.getChildAt(i) }, out);
			}
		}
	}

	// fix for bug #4741757
	public static class java_awt_Color_PersistenceDelegate extends
	AppletFriendlyDefaultPersistenceDelegate {
		protected Expression instantiate(Object oldInstance, Encoder out) {
			Color p = (Color) oldInstance;
			Object[] constructorArgs = new Object[] { new Integer(p.getRed()),
					new Integer(p.getGreen()), new Integer(p.getBlue()),
					new Integer(p.getAlpha()) };
			return new Expression(oldInstance, oldInstance.getClass(), "new",
					constructorArgs);
		}
	};

	// fix for bug #4741757
	public static class java_awt_geom_Point2D$Double_PersistenceDelegate extends
	AppletFriendlyDefaultPersistenceDelegate {
		public void initialize(Class type, Object oldInstance,
				Object newInstance, Encoder out) {
			super.initialize(type, oldInstance, newInstance, out);
			Point2D p = (Point2D) oldInstance;
			out
					.writeStatement(new Statement(oldInstance, "setLocation",
							new Object[] { new Double(p.getX()),
									new Double(p.getY()) }));
		}
	};

	public static class java_awt_geom_Rectangle2D_PersistenceDelegate extends
	AppletFriendlyDefaultPersistenceDelegate {
		protected Expression instantiate(Object oldInstance, Encoder out) {
			Rectangle2D p = (Rectangle2D) oldInstance;
			Object[] constructorArgs = new Object[] {
					new Double(p.getHeight()), new Double(p.getWidth()),
					new Double(p.getX()), new Double(p.getY()), };
			return new Expression(oldInstance, oldInstance.getClass(), "new",
					constructorArgs);
		}
	};

	public static class java_awt_Font_PersistenceDelegate extends
			AppletFriendlyDefaultPersistenceDelegate {
		protected Expression instantiate(Object oldInstance, Encoder out) {
			Font p = (Font) oldInstance;
			Object[] constructorArgs = new Object[] { new String(p.getName()),
					new Integer(p.getStyle()), new Integer(p.getSize()) };
			return new Expression(oldInstance, oldInstance.getClass(), "new",
					constructorArgs);
		}
	};

}
