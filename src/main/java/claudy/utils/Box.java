package claudy.utils;

public class Box
{
    public int minX;
    public int minY;
    public int minZ;
    public int maxX;
    public int maxY;
    public int maxZ;

    public Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
    {
        // Make the box compliant

        if (minX <= maxX) {
            this.minX = minX;
            this.maxX = maxX;
        } else {
            this.minX = maxX;
            this.maxX = minX;
        }

        if (minY <= maxY) {
            this.minY = minY;
            this.maxY = maxY;
        } else {
            this.minY = maxY;
            this.maxY = minY;
        }
        this.minY = this.minY < 0 ? 0 : this.minY;
        this.maxY = this.maxY > 256 ? 256 : this.maxY;

        if (minZ <= maxZ) {
            this.minZ = minZ;
            this.maxZ = maxZ;
        } else {
            this.minZ = maxZ;
            this.maxZ = minZ;
        }
    }

    public int getVolume()
    {
        return (this.maxX - this.minX + 1) * (this.maxY - this.minY + 1) * (this.maxZ - this.minZ + 1);
    }
}
