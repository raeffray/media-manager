db.createUser(
  {
    user: "woven-app",
    pwd: "qwaszx12",
    roles: [
      { role: "readWrite", db: "mediahub-medias" }
    ]
  }
);