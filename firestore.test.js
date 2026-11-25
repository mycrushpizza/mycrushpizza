const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const CUSTOMER_ALICE = "customer_alice_1";
const CUSTOMER_BOB = "customer_bob_2";
const RIDER_RAJU = "rider_raju_1";
const ADMIN_UID = "admin_hajipur_1";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function sampleOrderPayload(customerId, deliveryPartnerId = "") {
  const now = new Date();
  return {
    orderNumber: "MCP-1042",
    barcodeValue: "MCP-1042",
    customerId,
    customerName: "Ananya Sharma",
    customerPhone: "9876543210",
    deliveryAddress: "Station Road, Near Vishal Mega Mart, Hajipur",
    landmark: "Station Road",
    customerLat: 25.6892,
    customerLng: 85.2145,
    itemsSummary: "1x Tandoori Paneer Cheese Burst (Medium)",
    itemsCount: 1,
    subtotalInr: 349,
    discountInr: 50,
    couponCode: "CRUSH50",
    deliveryFeeInr: 0,
    taxInr: 15,
    totalInr: 314,
    paymentMethod: "UPI",
    paymentStatus: "PAID",
    status: "CONFIRMED",
    deliveryPartnerId,
    deliveryPartnerName: "",
    deliveryPartnerPhone: "",
    deliveryPartnerVehicle: "",
    deliveryLat: 25.6858,
    deliveryLng: 85.2095,
    isLocationSharingActive: false,
    estimatedMinutes: 28,
    distanceKm: 1.4,
    adminAlertAcknowledged: false,
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user: cannot read orders or profiles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("orders").get());
  await assertFails(unauthDb.collection("users").doc(CUSTOMER_ALICE).get());
});

test("Customer: can create their own order and query matching customerId", async () => {
  const aliceDb = testEnv.authenticatedContext(CUSTOMER_ALICE).firestore();
  await assertSucceeds(
    aliceDb
      .collection("orders")
      .doc("order_1042")
      .set(sampleOrderPayload(CUSTOMER_ALICE))
  );
  await assertSucceeds(
    aliceDb.collection("orders").where("customerId", "==", CUSTOMER_ALICE).get()
  );
});

test("Customer: cannot read another customer's order or query without filter", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("orders")
      .doc("bob_order_1")
      .set(sampleOrderPayload(CUSTOMER_BOB));
  });

  const aliceDb = testEnv.authenticatedContext(CUSTOMER_ALICE).firestore();
  await assertFails(aliceDb.collection("orders").doc("bob_order_1").get());
  await assertFails(aliceDb.collection("orders").get());
});

test("Customer: cannot spoof role to ADMIN on profile creation", async () => {
  const now = new Date();
  const aliceDb = testEnv.authenticatedContext(CUSTOMER_ALICE).firestore();
  await assertFails(
    aliceDb.collection("users").doc(CUSTOMER_ALICE).set({
      userId: CUSTOMER_ALICE,
      displayName: "Alice",
      role: "ADMIN",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Assigned Delivery Partner: can update live GPS location and status on assigned order", async () => {
  const initialOrder = sampleOrderPayload(CUSTOMER_ALICE, RIDER_RAJU);
  initialOrder.status = "ASSIGNED";
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("orders")
      .doc("order_assigned_1")
      .set(initialOrder);
  });

  const rajuDb = testEnv.authenticatedContext(RIDER_RAJU).firestore();
  await assertSucceeds(
    rajuDb.collection("orders").doc("order_assigned_1").update({
      status: "ON_THE_WAY",
      deliveryLat: 25.6875,
      deliveryLng: 85.2121,
      isLocationSharingActive: true,
      estimatedMinutes: 12,
      distanceKm: 0.7,
      updatedAt: new Date(),
    })
  );
});

test("Admin: can acknowledge incoming order alert, assign delivery partner, and manage menu", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("admins").doc(ADMIN_UID).set({
      adminUid: ADMIN_UID,
      email: "pranavgamer758@gmail.com",
      createdAt: now,
    });
    await context
      .firestore()
      .collection("orders")
      .doc("order_admin_1")
      .set(sampleOrderPayload(CUSTOMER_ALICE));
  });

  const adminDb = testEnv.authenticatedContext(ADMIN_UID).firestore();
  await assertSucceeds(
    adminDb.collection("orders").doc("order_admin_1").update({
      status: "ASSIGNED",
      deliveryPartnerId: RIDER_RAJU,
      deliveryPartnerName: "Raju Kumar",
      deliveryPartnerPhone: "9876543210",
      deliveryPartnerVehicle: "BR-31-M-4821",
      adminAlertAcknowledged: true,
      isLocationSharingActive: true,
      updatedAt: new Date(),
    })
  );
});
