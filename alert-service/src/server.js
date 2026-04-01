const { app, env } = require("./app");

app.listen(env.port, () => {
  console.log(`alert-service running on port ${env.port}`);
});
